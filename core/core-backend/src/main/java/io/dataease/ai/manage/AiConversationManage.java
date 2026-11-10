package io.dataease.ai.manage;

import io.dataease.ai.support.AiErrors;
import io.dataease.api.ai.conversation.AiConversationDtos.*;
import io.dataease.api.ai.runtime.*;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.utils.CommunityUtils;
import jakarta.annotation.PreDestroy;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** 有界临时会话；同步请求与受控异步执行共享身份、上下文及最终 Prompt 预算。 */
@Component
public class AiConversationManage implements AiFeature {
    private static final long SESSION_MILLIS = TimeUnit.MINUTES.toMillis(30);
    private static final int MAX_SESSIONS = 200, MAX_USER_SESSIONS = 4, MAX_TURNS = 50;
    private static final class State {
        final Session view;
        final AiAccess.Identity owner;
        final AiConversationScene.Context context;
        final List<Message> history = new ArrayList<>();
        volatile boolean closed;
        final Map<String, Run> runs = new LinkedHashMap<>();
        Run active;
        final AtomicBoolean executing = new AtomicBoolean();
        State(Session view, AiAccess.Identity owner, AiConversationScene.Context context) {
            this.view = view; this.owner = owner; this.context = context;
        }
    }
    private static final long LEASE_MILLIS = 15000;
    private static final class Run {
        final State state;
        String question;
        final AiRuntimeManage.Streaming stream;
        List<Event> events = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        long sequence;
        volatile long lease = System.currentTimeMillis() + LEASE_MILLIS;
        volatile String status = "RUNNING";
        Run(State state, String question, AiRuntimeManage.Streaming stream) { this.state = state; this.question = question; this.stream = stream; }
    }
    private final Map<String, State> sessions = new ConcurrentHashMap<>();
    private final Map<String, AiConversationScene> scenes;
    private final ObjectProvider<AiRuntimeManage> runtime;
    private final AiAccess access;
    private final ScheduledExecutorService expiry = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "de-ai-sessions"); thread.setDaemon(true); return thread;
    });
    public AiConversationManage(ObjectProvider<AiConversationScene> scenes, ObjectProvider<AiRuntimeManage> runtime, AiAccess access) {
        this.scenes = scenes.stream().collect(Collectors.toUnmodifiableMap(AiConversationScene::code, scene -> scene));
        this.runtime = runtime; this.access = access;
        expiry.scheduleWithFixedDelay(this::expire, 1, 1, TimeUnit.SECONDS);
    }
    @Override public boolean supportsStreaming() { return true; }
    @Override public String code() { return "development-assistant"; }
    public List<Resource> resources() {
        var owner = identity();
        return authorized(owner, () -> scene("sql").resources().stream().map(resource -> new Resource(resource.id(), resource.name())).toList());
    }
    public Session open(Open request) {
        AiAccess.Identity owner = identity();
        if (request == null || request.editorId() == null || request.editorId().isBlank() || request.editorId().length() > 100
                || request.resourceId() == null || !request.resourceId().matches("[0-9]{1,20}")
                || request.sql() != null && request.sql().length() > 30000) throw AiErrors.error("invalid");
        var scene = scene(request.scene());
        var context = authorized(owner, () -> {
            var resolved = scene.resolve(request.resourceId(), Objects.toString(request.sql(), ""));
            scene.authorize(resolved); return resolved;
        });
        String id = UUID.randomUUID().toString();
        var view = new Session(id, UUID.randomUUID().toString(), scene.code(), context.resourceId(), context.resourceName(), request.editorId(),
                System.currentTimeMillis() + SESSION_MILLIS);
        synchronized (sessions) {
            expire();
            if (sessions.size() >= MAX_SESSIONS || sessions.values().stream().filter(state -> same(state.owner, owner)).count() >= MAX_USER_SESSIONS)
                throw AiErrors.error("concurrency");
            sessions.put(id, new State(view, owner, context));
        }
        return view;
    }
    public Answer turn(String id, Turn request) {
        State state = owned(id);
        if (request == null || request.text() == null || request.text().isBlank() || request.text().length() > 30000
                || !Objects.equals(request.contextVersion(), state.view.contextVersion())) throw AiErrors.error("scope_changed");
        if (!state.executing.compareAndSet(false, true)) throw AiErrors.error("concurrency");
        try {
            authorized(state.owner, () -> { check(state); return null; });
            if (state.history.size() >= MAX_TURNS * 2) throw AiErrors.error("input_limit");
            var messages = new ArrayList<Message>();
            messages.add(new SystemMessage(state.context.instruction()));
            messages.addAll(state.history); messages.add(new UserMessage(request.text()));
            Prompt prompt = new Prompt(messages);
            var scope = new AiDataScope(id, state.view.contextVersion(), AiPromptFingerprint.of(prompt), false, null);
            var engine = runtime.getObject();
            var execution = engine.current(code(), scope);
            var response = engine.current(execution).call(prompt);
            // 权限及配置可能在模型执行时变化；交付和写入历史前再次复核。
            authorized(state.owner, () -> { check(state); return null; });
            if (System.currentTimeMillis() >= execution.deadline()) throw AiErrors.error("timeout");
            String text = response.getResult().getOutput().getText();
            if (text == null || text.isBlank()) throw AiErrors.error("response_invalid");
            state.history.add(new UserMessage(request.text())); state.history.add(new AssistantMessage(text));
            return new Answer(id, execution.executionId(), state.view.contextVersion(), "COMPLETED", text);
        } finally { state.executing.set(false); }
    }
    public Execution start(String id, Turn request) {
        State state = owned(id);
        if (request == null || request.text() == null || request.text().isBlank() || request.text().length() > 30000
                || !Objects.equals(request.contextVersion(), state.view.contextVersion())) throw AiErrors.error("scope_changed");
        synchronized (state) {
            if (!state.executing.compareAndSet(false, true)) throw AiErrors.error("concurrency");
            try {
                authorized(state.owner, () -> { check(state); return null; });
                if (state.history.size() >= MAX_TURNS * 2) throw AiErrors.error("input_limit");
                var messages = new ArrayList<Message>(); messages.add(new SystemMessage(state.context.instruction()));
                messages.addAll(state.history); messages.add(new UserMessage(request.text()));
                Prompt prompt = new Prompt(messages);
                var scope = new AiDataScope(id, state.view.contextVersion(), AiPromptFingerprint.of(prompt), false, null);
                var stream = runtime.getObject().stream(code(), scope, prompt);
                var run = new Run(state, request.text(), stream);
                // Terminal logs are bounded independently of the completed-history turn limit.
                for (Run old : state.runs.values()) {
                    old.question = null; old.text = new StringBuilder();
                    old.events = new ArrayList<>(old.events.stream().filter(event -> !"TEXT".equals(event.type())).toList());
                }
                while (state.runs.size() >= 50) state.runs.remove(state.runs.keySet().iterator().next());
                state.runs.put(stream.executionId(), run); state.active = run;
                try { stream.start(() -> progress(run), text -> append(run, text), () -> complete(run), failure -> fail(run, failure)); }
                catch (RuntimeException failure) { fail(run, failure); throw failure; }
                return new Execution(id, stream.executionId(), state.view.contextVersion(), run.status, stream.deadline());
            } catch (RuntimeException failure) { state.executing.set(false); throw failure; }
        }
    }
    public Events events(String id, String execution, long after) {
        State state = owned(id);
        synchronized (state) {
            Run run = run(state, execution);
            if (after < 0 || after > run.sequence) throw AiErrors.error("invalid");
            try {
                authorized(state.owner, () -> { check(state); run.stream.verifyDelivery(); return null; });
            } catch (RuntimeException failure) {
                invalidate(state); throw failure;
            }
            if (System.currentTimeMillis() >= run.stream.deadline()) terminate(run, "FAILED", "timeout");
            run.lease = System.currentTimeMillis() + LEASE_MILLIS;
            return view(run, after);
        }
    }
    public Events stop(String id, String execution) {
        State state = owned(id);
        synchronized (state) {
            Run run = run(state, execution);
            // Ownership is enough to clean up one's execution even after resource permission was lost.
            terminate(run, "CANCELLED", "cancelled");
            try { authorized(state.owner, () -> { check(state); return null; }); }
            catch (RuntimeException failure) { invalidate(state); throw failure; }
            return view(run, run.sequence - 1);
        }
    }
    private Run run(State state, String execution) {
        Run run = state.runs.get(execution); if (run == null) throw AiErrors.error("forbidden"); return run;
    }
    private Events view(Run run, long after) {
        var session = run.state.view;
        return new Events(session.sessionId(), run.stream.executionId(), session.contextVersion(), run.status, run.sequence,
                run.events.stream().filter(event -> event.sequence() > after).toList());
    }
    /** Parsing only fills a bounded private buffer; event delivery/history commit always verify current authority. */
    private void progress(Run run) {
        if (run.state.closed || !"RUNNING".equals(run.status)) throw AiErrors.error("cancelled");
        long now = System.currentTimeMillis();
        if (now >= run.stream.deadline()) throw AiErrors.error("timeout");
        if (now >= run.lease || now >= run.state.view.expiresAt()) throw AiErrors.error("scope_changed");
    }
    private void append(Run run, String text) {
        synchronized (run.state) {
            if (!"RUNNING".equals(run.status)) return;
            progress(run);
            if (run.events.size() >= 20000 || run.text.length() + text.length() > 262144) throw AiErrors.error("response_invalid");
            run.text.append(text); run.events.add(new Event(++run.sequence, "TEXT", text, null));
        }
    }
    private void complete(Run run) {
        synchronized (run.state) {
            if (!"RUNNING".equals(run.status)) return;
            try {
                authorized(run.state.owner, () -> { check(run.state); run.stream.verifyDelivery(); return null; });
                if (System.currentTimeMillis() >= run.stream.deadline()) throw AiErrors.error("timeout");
                if (run.text.isEmpty() || run.text.toString().isBlank()) throw AiErrors.error("response_invalid");
                run.state.history.add(new UserMessage(run.question)); run.state.history.add(new AssistantMessage(run.text.toString()));
                terminate(run, "COMPLETED", null);
            } catch (RuntimeException failure) { fail(run, failure); }
        }
    }
    private void fail(Run run, Throwable failure) {
        synchronized (run.state) {
            String error = failure instanceof AiErrors.Failure known ? known.safeCode() : "upstream_failed";
            if (Set.of("forbidden", "scope_changed", "conflict").contains(error)) invalidate(run.state);
            else terminate(run, "FAILED", error);
        }
    }
    private void terminate(Run run, String status, String error) {
        if (!"RUNNING".equals(run.status)) return;
        run.status = status; run.events.add(new Event(++run.sequence, status, null, error));
        if (run.state.active == run) { run.state.active = null; run.state.executing.set(false); }
        if (!"COMPLETED".equals(status)) run.stream.close();
    }
    private void invalidate(State state) {
        synchronized (state) {
            state.closed = true;
            if (state.active != null) terminate(state.active, "CANCELLED", "scope_changed");
            state.history.clear(); state.runs.clear(); sessions.remove(state.view.sessionId(), state);
        }
    }
    public void close(String id) { invalidate(owned(id)); }
    @Override public void authorize(AiExecutionContext context) {
        State state = sessions.get(context.scope().reference());
        if (state == null || !Objects.equals(context.scope().permissionVersion(), state.view.contextVersion())
                || !Objects.equals(context.actorId(), state.owner.actor()) || !Objects.equals(context.organizationId(), state.owner.organization())
                || !Objects.equals(context.proxyOrganizationId(), state.owner.proxy())) throw AiErrors.error("forbidden");
        try { authorized(state.owner, () -> { check(state); return null; }); }
        catch (RuntimeException failure) { invalidate(state); throw failure; }
    }
    @Override public void authorizeInput(AiExecutionContext context, Prompt prompt) { authorize(context); }
    private void check(State state) {
        if (state.closed || System.currentTimeMillis() >= state.view.expiresAt()) throw AiErrors.error("scope_changed");
        try {
            access.verify(state.owner, false);
            if (!same(access.capture(), state.owner)) throw AiErrors.error("forbidden");
            scene(state.view.scene()).authorize(state.context);
        } catch (RuntimeException failure) {
            invalidate(state);
            throw failure instanceof AiErrors.Failure ? failure : AiErrors.error("forbidden");
        }
    }
    private <T> T authorized(AiAccess.Identity owner, Supplier<T> operation) {
        // 不复制 Servlet 请求/事务。HTTP 传输线程只安装服务器保存的身份，并可靠清理。
        boolean request = RequestContextHolder.getRequestAttributes() != null;
        Long uid = V3UserUtil.getUid(); var proxy = V3UserUtil.getProxy(); var link = V3UserUtil.getLink();
        boolean community = CommunityUtils.isCommunityMode();
        V3UserUtil.clear(); CommunityUtils.clear(); V3UserUtil.setUid(owner.actor());
        if (owner.proxy() != null) V3UserUtil.setProxy(owner.proxy());
        CommunityUtils.setCommunityMode(!owner.deployment().organizations());
        try {
            access.verify(owner, false);
            if (!same(access.capture(), owner)) throw AiErrors.error("forbidden");
            T result = operation.get();
            access.verify(owner, false);
            return result;
        }
        finally {
            V3UserUtil.clear(); CommunityUtils.clear();
            if (request) {
                CommunityUtils.setCommunityMode(community);
                if (link != null) V3UserUtil.setLink(link);
                else { V3UserUtil.setUid(uid); if (proxy.isProxy()) V3UserUtil.setProxy(proxy.getProxyOid()); }
            }
        }
    }
    private State owned(String id) {
        State state = sessions.get(id);
        AiAccess.Identity owner;
        try { owner = identity(); }
        catch (RuntimeException failure) {
            // A trusted logged-in actor may always retire their own stale state, even when current access was revoked.
            if (state != null && RequestContextHolder.getRequestAttributes() != null && V3UserUtil.getLink() == null
                    && Objects.equals(V3UserUtil.getUid(), state.owner.actor())) invalidate(state);
            throw failure;
        }
        if (state == null) throw AiErrors.error("forbidden");
        if (!same(owner, state.owner)) {
            if (Objects.equals(owner.actor(), state.owner.actor())) invalidate(state);
            throw AiErrors.error("forbidden");
        }
        return state;
    }
    private AiAccess.Identity identity() {
        if (RequestContextHolder.getRequestAttributes() == null) throw AiErrors.error("forbidden");
        var identity = access.capture(); access.verify(identity, false); return identity;
    }
    private boolean same(AiAccess.Identity a, AiAccess.Identity b) { return Objects.equals(a, b); }
    private AiConversationScene scene(String code) {
        var scene = code == null ? null : scenes.get(code); if (scene == null) throw AiErrors.error("unsupported"); return scene;
    }
    private void expire() {
        long now = System.currentTimeMillis();
        sessions.forEach((id, state) -> {
            synchronized (state) {
                if (state.view.expiresAt() <= now) invalidate(state);
                else if (state.active != null) {
                    Run run = state.active;
                    if (now >= run.stream.deadline()) terminate(run, "FAILED", "timeout");
                    else if (now >= run.lease) invalidate(state);
                }
            }
        });
    }
    @PreDestroy public void destroy() { expiry.shutdownNow(); sessions.values().forEach(this::invalidate); sessions.clear(); }
}
