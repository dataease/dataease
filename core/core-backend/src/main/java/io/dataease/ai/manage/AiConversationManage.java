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

/** 有界临时会话。每轮创建请求绑定的模型代理，最终完整 Prompt 仍由既有底座限制。 */
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
        final AtomicBoolean executing = new AtomicBoolean();
        State(Session view, AiAccess.Identity owner, AiConversationScene.Context context) {
            this.view = view; this.owner = owner; this.context = context;
        }
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
        expiry.scheduleWithFixedDelay(this::expire, 1, 1, TimeUnit.MINUTES);
    }
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
    public void close(String id) {
        State state = owned(id); state.closed = true; sessions.remove(id, state);
    }
    @Override public void authorize(AiExecutionContext context) {
        State state = sessions.get(context.scope().reference());
        if (state == null || !Objects.equals(context.scope().permissionVersion(), state.view.contextVersion())
                || !Objects.equals(context.actorId(), state.owner.actor()) || !Objects.equals(context.organizationId(), state.owner.organization())
                || !Objects.equals(context.proxyOrganizationId(), state.owner.proxy())) throw AiErrors.error("forbidden");
        authorized(state.owner, () -> { check(state); return null; });
    }
    @Override public void authorizeInput(AiExecutionContext context, Prompt prompt) { authorize(context); }
    private void check(State state) {
        if (state.closed || System.currentTimeMillis() >= state.view.expiresAt()) throw AiErrors.error("scope_changed");
        try {
            access.verify(state.owner, false);
            if (!same(access.capture(), state.owner)) throw AiErrors.error("forbidden");
            scene(state.view.scene()).authorize(state.context);
        } catch (RuntimeException failure) {
            state.closed = true; sessions.remove(state.view.sessionId(), state);
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
        var owner = identity(); State state = sessions.get(id);
        if (state == null || !same(owner, state.owner)) throw AiErrors.error("forbidden");
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
        sessions.forEach((id, state) -> { if (state.view.expiresAt() <= now) { state.closed = true; sessions.remove(id, state); } });
    }
    @PreDestroy public void destroy() { expiry.shutdownNow(); sessions.values().forEach(state -> state.closed = true); sessions.clear(); }
}
