package io.dataease.ai.manage;

import io.dataease.ai.support.AiErrors;
import io.dataease.ai.support.AiToolProtocol;
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
public class AiConversationManage implements AiToolConversationFeature {
    private static final long SESSION_MILLIS = TimeUnit.MINUTES.toMillis(30);
    private static final int MAX_SESSIONS = 200, MAX_USER_SESSIONS = 4, MAX_TURNS = 50, MAX_SINGLE_RESULTS = 10;
    private static final class State {
        final Session view;
        final AiAccess.Identity owner;
        final AiConversationScene.Context context;
        final List<Message> history = new ArrayList<>();
        final List<AiToolProtocol.Message> toolHistory = new ArrayList<>();
        final Set<String> deliveredTasks = new HashSet<>();
        int turns;
        final boolean single;
        volatile boolean singleFinished;
        volatile boolean closed;
        final Map<String, Run> runs = new LinkedHashMap<>();
        Run active;
        final AtomicBoolean executing = new AtomicBoolean();
        State(Session view, AiAccess.Identity owner, AiConversationScene.Context context, boolean single) {
            this.view = view; this.owner = owner; this.context = context; this.single = single;
        }
    }
    private static final long LEASE_MILLIS = 15000;
    private static final class Run {
        final State state;
        String question;
        final AiRuntimeManage.StreamHandle stream;
        List<AiToolProtocol.Message> transcript = new ArrayList<>();
        final Set<String> toolIds = new HashSet<>(), toolRequests = new HashSet<>();
        int toolSteps;
        List<Event> events = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        Result result;
        long sequence;
        volatile long lease = System.currentTimeMillis() + LEASE_MILLIS;
        volatile String status = "RUNNING";
        Run(State state, String question, AiRuntimeManage.StreamHandle stream) { this.state = state; this.question = question; this.stream = stream; }
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
        return open(request, false);
    }
    private Session open(Open request, boolean single) {
        AiAccess.Identity owner = identity();
        if (request == null || request.editorId() == null || request.editorId().isBlank() || request.editorId().length() > 100
                || request.resourceId() != null && !request.resourceId().matches("[0-9]{1,20}|")
                || request.sql() != null && request.sql().length() > 30000) throw AiErrors.error("invalid");
        var scene = scene(request.scene());
        var context = authorized(owner, () -> {
            var resolved = scene.resolve(Objects.toString(request.resourceId(), ""), Objects.toString(request.sql(), ""));
            scene.authorize(resolved); return resolved;
        });
        String id = UUID.randomUUID().toString();
        var view = new Session(id, UUID.randomUUID().toString(), scene.code(), context.resourceId(), context.resourceName(), request.editorId(),
                System.currentTimeMillis() + SESSION_MILLIS, context.revision(), AiPromptFingerprint.of(new Prompt(Objects.toString(request.sql(), ""))));
        synchronized (sessions) {
            expire();
            // Results are temporary and bounded, but finished buttons no longer consume chat/run capacity.
            if (single) while (sessions.values().stream().filter(state -> state.singleFinished && same(state.owner, owner)).count() >= MAX_SINGLE_RESULTS)
                evictSingle(owner);
            while (sessions.size() >= MAX_SESSIONS && evictSingle(null)) { }
            if (sessions.size() >= MAX_SESSIONS || sessions.values().stream().filter(state -> same(state.owner, owner) && !state.singleFinished).count() >= MAX_USER_SESSIONS)
                throw AiErrors.error("concurrency");
            sessions.put(id, new State(view, owner, context, single));
        }
        return view;
    }
    private boolean evictSingle(AiAccess.Identity owner) {
        var oldest = sessions.values().stream().filter(state -> state.singleFinished && (owner == null || same(state.owner, owner)))
                .min(Comparator.comparingLong(state -> state.view.expiresAt()));
        oldest.ifPresent(this::invalidate);
        return oldest.isPresent();
    }
    /** A button owns an isolated editing activity, without requiring or reusing chat history. */
    public Execution invoke(String capability, Invoke request) {
        if (request == null || !Objects.equals(capability, scene(request.scene()).capability())) throw AiErrors.error("unsupported");
        Session session = open(new Open(request.scene(), request.resourceId(), request.editorId(), request.sql()), true);
        try {
            Execution run = start(session.sessionId(), new Turn(session.contextVersion(), request.text()));
            return new Execution(run.sessionId(), run.executionId(), run.contextVersion(), run.status(), run.deadline(), session);
        }
        catch (RuntimeException failure) { State state = sessions.get(session.sessionId()); if (state != null) invalidate(state); throw failure; }
    }
    /** Only a still-open, same-owner editing activity may become a background result's origin. */
    public void validateTaskOrigin(io.dataease.api.ai.task.AiTaskDtos.Origin origin,
                                   io.dataease.api.ai.task.AiTaskDtos.Submit input, AiConversationScene.Context context) {
        if (origin == null || origin.sessionId() == null) throw AiErrors.error("scope_changed");
        State state = owned(origin.sessionId());
        synchronized (state) {
            authorized(state.owner, () -> { check(state); return null; });
            var view = state.view;
            if (state.single || !Objects.equals(view.contextVersion(), origin.contextVersion())
                    || !Objects.equals(view.scene(), input.scene()) || !Objects.equals(view.editorId(), input.editorId())
                    || !Objects.equals(view.resourceId(), context.resourceId()) || !Objects.equals(view.resourceVersion(), context.revision())
                    || !Objects.equals(view.draftVersion(), AiPromptFingerprint.of(new Prompt(Objects.toString(input.sql(), ""))))) throw AiErrors.error("scope_changed");
        }
    }
    /** Adds a real model turn, including validated candidate SQL, without restoring any closed session. */
    public void appendTaskResult(io.dataease.api.ai.task.AiTaskDtos.Origin origin,
                                 io.dataease.api.ai.task.AiTaskDtos.Submit input, AiConversationScene.Context context,
                                 String attemptId, Result result) {
        validateTaskOrigin(origin, input, context);
        State state = owned(origin.sessionId());
        synchronized (state) {
            authorized(state.owner, () -> { check(state); return null; });
            if (state.deliveredTasks.contains(attemptId)) return;
            if (state.executing.get()) throw AiErrors.error("concurrency");
            if (state.turns >= MAX_TURNS) throw AiErrors.error("input_limit");
            String answer = result.text() + (result.sql() == null ? "" : "\nSQL:\n" + result.sql());
            if (state.toolHistory.isEmpty()) {
                state.history.add(new UserMessage(input.text())); state.history.add(new AssistantMessage(answer));
            } else {
                state.toolHistory.add(AiToolProtocol.Message.text("user", input.text()));
                state.toolHistory.add(AiToolProtocol.Message.text("assistant", answer));
            }
            state.turns++; state.deliveredTasks.add(attemptId);
        }
    }
    public Answer turn(String id, Turn request) {
        State state = owned(id);
        if (state.single) throw AiErrors.error("unsupported");
        if (request == null || request.text() == null || request.text().isBlank() || request.text().length() > 30000
                || !Objects.equals(request.contextVersion(), state.view.contextVersion())) throw AiErrors.error("scope_changed");
        if (!state.executing.compareAndSet(false, true)) throw AiErrors.error("concurrency");
        try {
            authorized(state.owner, () -> { check(state); return null; });
            if (state.turns >= MAX_TURNS) throw AiErrors.error("input_limit");
            if (!state.toolHistory.isEmpty()) throw AiErrors.error("unsupported");
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
            state.history.add(new UserMessage(request.text())); state.history.add(new AssistantMessage(text)); state.turns++;
            return new Answer(id, execution.executionId(), state.view.contextVersion(), "COMPLETED", text);
        } finally { state.executing.set(false); }
    }
    public Execution start(String id, Turn request) {
        State state = owned(id);
        if (request == null || request.text() == null || request.text().isBlank() || request.text().length() > 30000
                || !Objects.equals(request.contextVersion(), state.view.contextVersion())) throw AiErrors.error("scope_changed");
        synchronized (state) {
            if (state.single && !state.runs.isEmpty()) throw AiErrors.error("unsupported");
            if (!state.executing.compareAndSet(false, true)) throw AiErrors.error("concurrency");
            try {
                authorized(state.owner, () -> { check(state); return null; });
                if (state.turns >= MAX_TURNS) throw AiErrors.error("input_limit");
                var messages = new ArrayList<Message>(); messages.add(new SystemMessage(state.context.instruction()));
                messages.addAll(state.history); messages.add(new UserMessage(request.text()));
                Prompt prompt = new Prompt(messages);
                var scope = new AiDataScope(id, state.view.contextVersion(), AiPromptFingerprint.of(prompt), false, null);
                var definitions = scene(state.view.scene()).tools(state.context);
                var input = toolInput(state, request.text(), definitions);
                AiRuntimeManage.StreamHandle stream = definitions.isEmpty() ? runtime.getObject().stream(code(), scope, prompt)
                        : runtime.getObject().streamTools(code(), new AiDataScope(id, state.view.contextVersion(), AiToolProtocol.fingerprint(input), false, null), input);
                var run = new Run(state, request.text(), stream);
                if (stream instanceof AiRuntimeManage.ToolStreaming) run.transcript.addAll(input.messages());
                // Terminal logs are bounded independently of the completed-history turn limit.
                for (Run old : state.runs.values()) {
                    old.question = null; old.text = new StringBuilder(); old.result = null;
                    old.events = new ArrayList<>(old.events.stream().filter(event -> !"TEXT".equals(event.type()) && event.tool() == null).toList());
                }
                while (state.runs.size() >= 50) state.runs.remove(state.runs.keySet().iterator().next());
                state.runs.put(stream.executionId(), run); state.active = run;
                try {
                    if (stream instanceof AiRuntimeManage.ToolStreaming tools) tools.send(input, () -> progress(run), text -> append(run, text), step -> toolStep(run, step), failure -> fail(run, failure));
                    else ((AiRuntimeManage.Streaming) stream).start(() -> progress(run), text -> append(run, text), () -> complete(run), failure -> fail(run, failure));
                }
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
                run.events.stream().filter(event -> event.sequence() > after).toList(), run.result);
    }
    /** Checks authority and the immutable source revision; the editor still performs its own draft CAS. */
    public Result checkApplication(String id, String execution, Application request) {
        State state = owned(id);
        synchronized (state) {
            authorized(state.owner, () -> { check(state); return null; });
            Run run = run(state, execution);
            run.stream.verifyDelivery();
            if (request == null || !Objects.equals(request.contextVersion(), state.view.contextVersion())
                    || !Objects.equals(request.draftVersion(), state.view.draftVersion())) throw AiErrors.error("scope_changed");
            if (!"COMPLETED".equals(run.status) || run.result == null || !"SQL_SUGGESTION".equals(run.result.type())) throw AiErrors.error("invalid");
            return run.result;
        }
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
                progress(run);
                if (run.text.isEmpty() || run.text.toString().isBlank()) throw AiErrors.error("response_invalid");
                if (run.state.single) {
                    // One-shot callers retain only their result/lease, never a conversational transcript.
                    run.state.history.clear(); run.state.toolHistory.clear();
                } else if (run.stream instanceof AiRuntimeManage.ToolStreaming) {
                    run.state.history.clear(); run.state.toolHistory.clear(); run.state.toolHistory.addAll(run.transcript.subList(1, run.transcript.size()));
                } else {
                    run.state.history.add(new UserMessage(run.question)); run.state.history.add(new AssistantMessage(run.text.toString()));
                }
                run.state.turns++;
                var target = run.state.view;
                run.result = scene(target.scene()).result(run.state.context, run.text.toString(),
                        run.events.stream().map(Event::tool).filter(Objects::nonNull).toList(), target.editorId(), target.draftVersion());
                terminate(run, "COMPLETED", null);
            } catch (RuntimeException failure) { fail(run, failure); }
        }
    }
    private AiToolProtocol.Input toolInput(State state, String question, List<AiToolProtocol.Definition> definitions) {
        var messages = new ArrayList<AiToolProtocol.Message>();
        messages.add(AiToolProtocol.Message.text("system", state.context.instruction()));
        // A session's text-only synchronous history remains compatible when its first tool run starts.
        if (state.toolHistory.isEmpty()) for (Message message : state.history)
            messages.add(AiToolProtocol.Message.text(message.getMessageType().getValue(), message.getText()));
        else messages.addAll(state.toolHistory);
        messages.add(AiToolProtocol.Message.text("user", question));
        return new AiToolProtocol.Input(messages, definitions);
    }
    @Override public void authorizeToolInput(AiExecutionContext context, AiToolProtocol.Input input) {
        authorize(context);
        State state = sessions.get(context.scope().reference());
        if (state == null || !scene(state.view.scene()).tools(state.context).equals(input.tools())) throw AiErrors.error("scope_changed");
    }
    private void toolEvent(Run run, AiToolProtocol.Call call, String status, Map<String, String> arguments, String result) {
        synchronized (run.state) {
            progress(run);
            if (run.events.size() >= 20000) throw AiErrors.error("output_limit");
            run.events.add(new Event(++run.sequence, "TOOL", null, null, new Tool(call.id(), call.name(), status, arguments, result)));
        }
    }
    private void toolStep(Run run, AiToolProtocol.Step step) {
        try {
            var stream = (AiRuntimeManage.ToolStreaming) run.stream;
            progress(run);
            authorized(run.state.owner, () -> { check(run.state); stream.verifyStep(); return null; });
            if (step.calls().isEmpty()) {
                synchronized (run.state) { progress(run); run.transcript.add(AiToolProtocol.Message.assistant(step)); }
                complete(run); return;
            }
            if (step.calls().size() != 1 || run.toolSteps >= AiToolProtocol.MAX_MODEL_STEPS - 1) throw AiErrors.error("output_limit");
            var call = step.calls().get(0); var scene = scene(run.state.view.scene());
            var definition = scene.tools(run.state.context).stream().filter(tool -> tool.name().equals(call.name())).findFirst().orElseThrow(() -> AiErrors.error("unsupported"));
            var arguments = AiToolProtocol.arguments(definition, call);
            String canonical = call.name() + ":" + new TreeMap<>(arguments);
            synchronized (run.state) {
                progress(run);
                if (!run.toolIds.add(call.id()) || !run.toolRequests.add(canonical)) throw AiErrors.error("output_limit");
                run.toolSteps++;
            }
            toolEvent(run, call, "STARTED", arguments, null);
            String result;
            try {
                result = authorized(run.state.owner, () -> {
                    progress(run); check(run.state); stream.verifyStep();
                    toolEvent(run, call, "RUNNING", arguments, null);
                    String value = scene.execute(run.state.context, call.name(), arguments, () -> progress(run), run.stream.deadline());
                    progress(run); check(run.state); stream.verifyStep(); return value;
                });
                progress(run); stream.result(result);
            } catch (RuntimeException failure) {
                if (!(failure instanceof AiErrors.Failure known) || !Set.of("forbidden", "scope_changed", "conflict").contains(known.safeCode()))
                    toolEvent(run, call, "FAILED", arguments, null);
                throw failure;
            }
            synchronized (run.state) {
                progress(run); run.transcript.add(AiToolProtocol.Message.assistant(step)); run.transcript.add(AiToolProtocol.Message.result(call, result));
                toolEvent(run, call, "SUCCEEDED", arguments, result);
            }
            // The previous response/permit is already closed. Enqueue the next step, without waiting on this worker.
            var input = new AiToolProtocol.Input(run.transcript, scene.tools(run.state.context));
            stream.send(input, () -> progress(run), text -> append(run, text), next -> toolStep(run, next), failure -> fail(run, failure));
        } catch (RuntimeException failure) { fail(run, failure); }
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
        if (run.state.single) run.state.singleFinished = true;
        if (run.state.active == run) { run.state.active = null; run.state.executing.set(false); }
        run.question = null; run.transcript = new ArrayList<>(); run.toolIds.clear(); run.toolRequests.clear();
        run.stream.close();
    }
    private void invalidate(State state) {
        synchronized (state) {
            state.closed = true;
            if (state.active != null) terminate(state.active, "CANCELLED", "scope_changed");
            state.history.clear(); state.toolHistory.clear(); state.runs.clear(); sessions.remove(state.view.sessionId(), state);
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
        return new AiIdentityScope(access).call(owner, operation);
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
