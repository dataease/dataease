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
    private record OperationKey(AiAccess.Identity owner, String kind, String key) { }
    private static final class OperationBinding {
        final String fingerprint;
        final long expiresAt;
        volatile State state;
        volatile Task task;
        OperationBinding(String fingerprint, State state, Task task) {
            this.fingerprint = fingerprint; this.state = state; this.task = task; expiresAt = state.view.expiresAt();
        }
    }
    private static final class State {
        final Session view;
        final AiAccess.Identity owner;
        final AiConversationScene.Context context;
        final List<Message> history = new ArrayList<>();
        final List<AiToolProtocol.Message> toolHistory = new ArrayList<>();
        final Set<String> deliveredTasks = new HashSet<>();
        int turns;
        long historyVersion;
        volatile boolean retired;
        Invoke invocation;
        final Map<String, Task> tasks = new LinkedHashMap<>();
        final Set<OperationKey> operations = new HashSet<>();
        Open opening;
        final boolean single;
        final String usageRevision;
        volatile boolean singleFinished;
        volatile boolean closed;
        final Map<String, Run> runs = new LinkedHashMap<>();
        Run active;
        final AtomicBoolean executing = new AtomicBoolean();
        State(Session view, AiAccess.Identity owner, AiConversationScene.Context context, boolean single, String usageRevision) {
            this.view = view; this.owner = owner; this.context = context; this.single = single; this.usageRevision = usageRevision;
        }
    }
    private static final class Task {
        final String id = UUID.randomUUID().toString();
        final State state;
        final Turn request;
        final Prompt prompt;
        final AiToolProtocol.Input input;
        final long historyVersion;
        final Map<String, Run> retries = new HashMap<>();
        Run first, latest;
        Task(State state, Turn request, Prompt prompt, AiToolProtocol.Input input, long historyVersion) {
            this.state = state; this.request = request; this.prompt = prompt; this.input = input; this.historyVersion = historyVersion;
        }
    }
    private static final long LEASE_MILLIS = 15000;
    private static final class Run {
        final State state;
        final Task task;
        final String attemptId = UUID.randomUUID().toString();
        final String sourceAttemptId;
        final int attemptNumber;
        boolean replayUnsafe;
        String question;
        final AiRuntimeManage.StreamHandle stream;
        List<AiToolProtocol.Message> transcript = new ArrayList<>();
        final Set<String> toolIds = new HashSet<>(), toolRequests = new HashSet<>();
        int toolSteps;
        final Set<String> invalidRequests = new HashSet<>();
        List<Event> events = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        Result result;
        long sequence;
        volatile long lease = System.currentTimeMillis() + LEASE_MILLIS;
        volatile String status = "RUNNING";
        Run(State state, Task task, AiRuntimeManage.StreamHandle stream) {
            this.state = state; this.task = task; this.question = task.request.text(); this.stream = stream;
            sourceAttemptId = task.latest == null ? null : task.latest.attemptId;
            attemptNumber = task.latest == null ? 1 : task.latest.attemptNumber + 1;
        }
    }
    private final Map<String, State> sessions = new ConcurrentHashMap<>();
    // Closed operations keep only owner/key/hash/expiry; never a prompt, history or result.
    private final Map<OperationKey, OperationBinding> operations = new ConcurrentHashMap<>();
    private String fingerprint(Object... values) {
        var input = new StringBuilder();
        for (Object value : values) { String text = value == null ? null : value.toString(); input.append(text == null ? -1 : text.length()).append(':'); if (text != null) input.append(text); }
        return AiPromptFingerprint.of(new Prompt(input.toString()));
    }
    private String invocation(Invoke request) { return fingerprint("invoke", request.scene(), request.resourceId(), request.editorId(), request.sql(), request.text()); }
    private OperationBinding operation(AiAccess.Identity owner, String kind, String key, String fingerprint) {
        if (key == null) return null;
        var binding = operations.get(new OperationKey(owner, kind, key));
        if (binding != null) {
            if (!binding.fingerprint.equals(fingerprint)) throw AiErrors.error("conflict");
            if (binding.state == null || System.currentTimeMillis() >= binding.expiresAt) throw AiErrors.error("scope_changed");
        }
        return binding;
    }
    private OperationBinding bind(State state, String kind, String key, String fingerprint, Task task) {
        if (key == null) return null;
        synchronized (operations) {
            var existing = operation(state.owner, kind, key, fingerprint);
            if (existing != null) return existing;
            if (operations.size() >= MAX_SESSIONS * (MAX_TURNS + 1)) throw AiErrors.error("concurrency");
            var operationKey = new OperationKey(state.owner, kind, key);
            var binding = new OperationBinding(fingerprint, state, task); operations.put(operationKey, binding); state.operations.add(operationKey); return binding;
        }
    }
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
        synchronized (sessions) {
        if (request == null || request.editorId() == null || request.editorId().isBlank() || request.editorId().length() > 100
                || request.resourceId() != null && !request.resourceId().matches("[0-9]{1,20}|")
                || request.sql() != null && request.sql().length() > 30000) throw AiErrors.error("invalid");
        key(request.openKey());
        String opening = fingerprint("open", single, request.scene(), request.resourceId(), request.editorId(), request.sql());
        var priorOpening = operation(owner, "open", request.openKey(), opening);
        if (priorOpening != null) { State previous = priorOpening.state; if (previous == null) throw AiErrors.error("scope_changed"); check(previous); return previous.view; }
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
            // Replaced idle editors keep only bounded result/task records; no history or interaction quota.
            if (!single) for (State previous : sessions.values()) synchronized (previous) {
                if (!previous.single && !previous.retired && same(previous.owner, owner)
                        && previous.view.editorId().equals(request.editorId()) && !previous.executing.get()) {
                    previous.retired = true; previous.history.clear(); previous.toolHistory.clear();
                    for (Run old : previous.runs.values()) { old.text = new StringBuilder(); old.events = new ArrayList<>(); }
                }
            }
            while (sessions.values().stream().filter(state -> (state.singleFinished || state.retired) && !state.executing.get() && same(state.owner, owner)).count() >= MAX_SINGLE_RESULTS)
                evictSingle(owner);
            while (sessions.size() >= MAX_SESSIONS && evictSingle(null)) { }
            if (sessions.size() >= MAX_SESSIONS || sessions.values().stream().filter(state -> same(state.owner, owner) && occupiesActivity(state)).count() >= MAX_USER_SESSIONS)
                throw AiErrors.error("concurrency");
            State created = new State(view, owner, context, single, runtime.getObject().usageRevision(owner)); created.opening = request; bind(created, "open", request.openKey(), opening, null); sessions.put(id, created);
        }
        return view;
        }
    }
    private boolean occupiesActivity(State state) { return state.executing.get() || !state.singleFinished && !state.retired; }
    private boolean evictSingle(AiAccess.Identity owner) {
        var oldest = sessions.values().stream().filter(state -> (state.singleFinished || state.retired) && !state.executing.get() && (owner == null || same(state.owner, owner)))
                .min(Comparator.comparingLong(state -> state.view.expiresAt()));
        oldest.ifPresent(this::invalidate);
        return oldest.isPresent();
    }
    /** A button owns an isolated editing activity, without requiring or reusing chat history. */
    public Execution invoke(String capability, Invoke request) {
        if (request == null || !Objects.equals(capability, scene(request.scene()).capability())) throw AiErrors.error("unsupported");
        key(request.submissionKey());
        var owner = identity();
        synchronized (sessions) {
            var prior = operation(owner, "task", request.submissionKey(), invocation(request));
            if (prior != null) {
                Task task = prior.task; if (task == null) throw AiErrors.error("scope_changed");
                synchronized (task.state) { check(task.state); return execution(task.first, true); }
            }
            Session session = open(new Open(request.scene(), request.resourceId(), request.editorId(), request.sql()), true);
            try {
                State state = sessions.get(session.sessionId()); state.invocation = request;
                start(session.sessionId(), new Turn(session.contextVersion(), request.text(), request.submissionKey()));
                return execution(state.tasks.values().iterator().next().first, true);
            } catch (RuntimeException failure) { State state = sessions.get(session.sessionId()); if (state != null) invalidate(state); throw failure; }
        }
    }
    private void key(String key) { if (key != null && (key.isBlank() || key.length() > 100)) throw AiErrors.error("invalid"); }
    private boolean retryEligible(Run run) { return !run.replayUnsafe && Set.of("FAILED", "CANCELLED").contains(run.status); }
    private RecoveryProgress recoveryProgress(Run run) {
        var progress = run.stream.recovery().progress();
        return new RecoveryProgress(progress.stepId(), progress.stepKind(), progress.technicalRetries(), progress.corrections(), progress.reason());
    }
    private Execution execution(Run run, boolean context) {
        var state = run.state;
        return new Execution(state.view.sessionId(), run.stream.executionId(), state.view.contextVersion(), run.status, run.stream.deadline(),
                context ? state.view : null, run.task.id, run.attemptId, run.attemptNumber, run.sourceAttemptId, retryEligible(run), recoveryProgress(run));
    }
    /** Only a still-open, same-owner editing activity may become a background result's origin. */
    public void validateTaskOrigin(io.dataease.api.ai.task.AiTaskDtos.Origin origin,
                                   io.dataease.api.ai.task.AiTaskDtos.Submit input, AiConversationScene.Context context) {
        if (origin == null || origin.sessionId() == null) throw AiErrors.error("scope_changed");
        State state = owned(origin.sessionId());
        synchronized (state) {
            authorized(state.owner, () -> { check(state); return null; });
            var view = state.view;
            if (state.single || state.retired || !Objects.equals(view.contextVersion(), origin.contextVersion())
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
            state.turns++; state.historyVersion++; state.deliveredTasks.add(attemptId);
        }
    }
    public Answer turn(String id, Turn request) {
        State state = owned(id);
        if (state.single) throw AiErrors.error("unsupported");
        if (request == null || request.text() == null || request.text().isBlank() || request.text().length() > 30000
                || !Objects.equals(request.contextVersion(), state.view.contextVersion())) throw AiErrors.error("scope_changed");
        synchronized (sessions) {
            synchronized (state) {
                if (state.retired) throw AiErrors.error("scope_changed");
                if (!state.executing.compareAndSet(false, true)) throw AiErrors.error("concurrency");
            }
        }
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
            if (state.retired) throw AiErrors.error("scope_changed");
            state.history.add(new UserMessage(request.text())); state.history.add(new AssistantMessage(text)); state.turns++; state.historyVersion++;
            return new Answer(id, execution.executionId(), state.view.contextVersion(), "COMPLETED", text);
        } finally { state.executing.set(false); }
    }
    public Execution start(String id, Turn request) {
        State state = owned(id);
        if (request == null || request.text() == null || request.text().isBlank() || request.text().length() > 30000
                || !Objects.equals(request.contextVersion(), state.view.contextVersion())) throw AiErrors.error("scope_changed");
        key(request.submissionKey());
        synchronized (sessions) {
        synchronized (state) {
            authorized(state.owner, () -> { check(state); return null; });
            String accepted = state.invocation == null ? fingerprint("chat", id, request.contextVersion(), request.text())
                    : invocation(new Invoke(state.invocation.scene(), state.invocation.resourceId(), state.invocation.editorId(), state.invocation.sql(), request.text(), request.submissionKey()));
            var prior = operation(state.owner, "task", request.submissionKey(), accepted);
            if (prior != null) {
                if (prior.state != state || prior.task == null) throw AiErrors.error("conflict");
                return execution(prior.task.first, false);
            }
            if (state.retired || state.single && !state.runs.isEmpty()) throw AiErrors.error("unsupported");
            if (state.turns >= MAX_TURNS || state.tasks.size() >= 50) throw AiErrors.error("input_limit");
            var messages = new ArrayList<Message>(); messages.add(new SystemMessage(state.context.instruction()));
            messages.addAll(state.history); messages.add(new UserMessage(request.text()));
            var definitions = scene(state.view.scene()).tools(state.context);
            var task = new Task(state, request, new Prompt(List.copyOf(messages)), toolInput(state, request.text(), definitions), state.historyVersion);
            var binding = bind(state, "task", request.submissionKey(), accepted, task);
            if (binding != null && binding.task != task) throw AiErrors.error("conflict");
            state.tasks.put(task.id, task);
            try { return launch(state, task); }
            catch (RuntimeException failure) {
                if (task.first == null) {
                    state.tasks.remove(task.id);
                    if (binding != null) { var operationKey = new OperationKey(state.owner, "task", request.submissionKey()); operations.remove(operationKey, binding); state.operations.remove(operationKey); }
                }
                throw failure;
            }
        }
        }
    }
    public Execution retry(String id, String executionId, Retry request) {
        State state = owned(id);
        synchronized (sessions) {
        synchronized (state) {
            authorized(state.owner, () -> { check(state); return null; });
            if (request == null || request.retryKey() == null || request.expectedAttemptId() == null) throw AiErrors.error("invalid");
            key(request.retryKey());
            Run source = run(state, executionId); Task task = source.task;
            Run replay = task.retries.get(request.retryKey());
            if (replay != null) {
                if (!Objects.equals(replay.sourceAttemptId, request.expectedAttemptId())) throw AiErrors.error("conflict");
                return execution(replay, state.single);
            }
            if (!Objects.equals(source.attemptId, request.expectedAttemptId()) || task.latest != source) throw AiErrors.error("conflict");
            if (!retryEligible(source)) throw AiErrors.error("unsupported");
            var result = launch(state, task); task.retries.put(request.retryKey(), task.latest); return result;
        }
        }
    }
    /** Caller holds sessions then state: releasing and reacquiring an activity slot cannot race Open. */
    private Execution launch(State state, Task task) {
        if ((state.singleFinished || state.retired) && sessions.values().stream()
                .filter(current -> same(current.owner, state.owner) && occupiesActivity(current)).count() >= MAX_USER_SESSIONS)
            throw AiErrors.error("concurrency");
        if (state.runs.size() >= 50) throw AiErrors.error("input_limit");
        if (!state.executing.compareAndSet(false, true)) throw AiErrors.error("concurrency");
        try {
            authorized(state.owner, () -> { check(state); return null; });
            String id = state.view.sessionId();
            var scope = new AiDataScope(id, state.view.contextVersion(), AiPromptFingerprint.of(task.prompt), false, null);
            AiRuntimeManage.StreamHandle stream = task.input.tools().isEmpty() ? runtime.getObject().stream(code(), scope, task.prompt)
                    : runtime.getObject().streamTools(code(), new AiDataScope(id, state.view.contextVersion(), AiToolProtocol.fingerprint(task.input), false, null), task.input);
            var run = new Run(state, task, stream);
            stream.recovery().link(new AiExecutionLink(task.id, run.attemptId, run.attemptNumber, run.sourceAttemptId));
            if (stream instanceof AiRuntimeManage.ToolStreaming) run.transcript.addAll(task.input.messages());
            for (Run old : state.runs.values()) {
                old.question = null; old.text = new StringBuilder();
                old.events = new ArrayList<>(old.events.stream().filter(event -> !"TEXT".equals(event.type()) && event.tool() == null).toList());
            }
            state.runs.put(stream.executionId(), run); state.active = run; state.singleFinished = false;
            task.latest = run; if (task.first == null) task.first = run;
            try {
                if (stream instanceof AiRuntimeManage.ToolStreaming tools) tools.send(task.input, () -> progress(run), text -> append(run, text), step -> toolStep(run, step), failure -> fail(run, failure));
                else ((AiRuntimeManage.Streaming) stream).start(() -> progress(run), text -> append(run, text), () -> complete(run), failure -> fail(run, failure));
            } catch (RuntimeException failure) { fail(run, failure); throw failure; }
            return execution(run, state.single);
        } catch (RuntimeException failure) { state.executing.set(false); throw failure; }
    }
    public Events events(String id, String execution, long after) {
        State state = owned(id);
        synchronized (state) {
            Run run = run(state, execution);
            if (after < 0 || after > run.sequence) throw AiErrors.error("invalid");
            try {
                authorized(state.owner, () -> {
                    check(state);
                    // Failed/cancelled attempts retain their safe task identity for explicit new attempts.
                    if (Set.of("RUNNING", "COMPLETED").contains(run.status)) run.stream.verifyDelivery();
                    return null;
                });
            } catch (RuntimeException failure) {
                if (failure instanceof AiErrors.Failure known && Set.of("conflict", "disabled", "organization_disabled", "model_unavailable", "configuration_unavailable").contains(known.safeCode())) {
                    if (!"RUNNING".equals(run.status)) throw failure;
                    run.text = new StringBuilder(); run.events.clear(); run.result = null;
                    terminate(run, "FAILED", known.safeCode());
                } else { invalidate(state); throw failure; }
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
                run.events.stream().filter(event -> event.sequence() > after).toList(), run.result, run.task.id, run.attemptId, run.attemptNumber, run.sourceAttemptId, retryEligible(run), recoveryProgress(run));
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
                } else if (!run.state.retired && run.task.historyVersion == run.state.historyVersion && run.stream instanceof AiRuntimeManage.ToolStreaming) {
                    run.state.history.clear(); run.state.toolHistory.clear(); run.state.toolHistory.addAll(run.transcript.subList(1, run.transcript.size()));
                } else if (!run.state.retired && run.task.historyVersion == run.state.historyVersion) {
                    run.state.history.add(new UserMessage(run.question)); run.state.history.add(new AssistantMessage(run.text.toString()));
                }
                run.state.turns++;
                if (!run.state.single && !run.state.retired && run.task.historyVersion == run.state.historyVersion) run.state.historyVersion++;
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
            Map<String, String> arguments;
            try { arguments = AiToolProtocol.arguments(definition, call); }
            catch (AiErrors.Failure malformed) {
                if (!"invalid".equals(malformed.safeCode())) throw malformed;
                String feedback = AiToolProtocol.correction(call, stream.recovery(), run.invalidRequests); stream.result(feedback);
                synchronized (run.state) {
                    progress(run); run.transcript.add(AiToolProtocol.Message.assistant(step)); run.transcript.add(AiToolProtocol.Message.result(call, feedback));
                }
                stream.send(new AiToolProtocol.Input(run.transcript, run.task.input.tools()), () -> progress(run), text -> append(run, text), next -> toolStep(run, next), failure -> fail(run, failure));
                return;
            }
            String canonical = call.name() + ":" + new TreeMap<>(arguments);
            synchronized (run.state) {
                progress(run);
                if (!run.toolIds.add(call.id()) || !run.toolRequests.add(canonical)) throw AiErrors.error("output_limit");
                run.toolSteps++;
            }
            toolEvent(run, call, "STARTED", arguments, null);
            String result;
            try {
                if (definition.retrySafety() == AiToolProtocol.RetrySafety.UNKNOWN) run.replayUnsafe = true;
                java.util.function.Supplier<String> execute = () -> authorized(run.state.owner, () -> {
                    progress(run); check(run.state); stream.verifyStep();
                    toolEvent(run, call, "RUNNING", arguments, null);
                    String value = scene.execute(run.state.context, call.name(), arguments, () -> progress(run), run.stream.deadline());
                    progress(run); check(run.state); stream.verifyStep(); return value;
                });
                result = stream.recovery().execute("TOOL", stream.deadline(), () -> progress(run),
                        () -> definition.retrySafety() == AiToolProtocol.RetrySafety.UNKNOWN, () -> { }, () -> stream.recovery().tool(call.id(), call.name(), arguments, execute));
                progress(run); stream.result(result);
                if (AiToolProtocol.invalidResult(result) && !stream.recovery().correct(canonical)) throw AiErrors.error("invalid");
            } catch (RuntimeException failure) {
                if (!(failure instanceof AiErrors.Failure known) || !Set.of("forbidden", "scope_changed", "conflict").contains(known.safeCode()))
                    toolEvent(run, call, "FAILED", arguments, null);
                if (run.replayUnsafe) throw AiErrors.error("outcome_unknown");
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
            if (Set.of("forbidden", "scope_changed").contains(error)) invalidate(run.state);
            else terminate(run, "FAILED", error);
        }
    }
    private void terminate(Run run, String status, String error) {
        if (!"RUNNING".equals(run.status)) return;
        run.status = status; run.events.add(new Event(++run.sequence, status, null, error));
        if (run.state.single) run.state.singleFinished = true;
        if (run.state.active == run) { run.state.active = null; run.state.executing.set(false); }
        run.question = null; run.transcript = new ArrayList<>(); run.toolIds.clear(); run.toolRequests.clear();
        run.stream.finishAudit(status); run.stream.close();
    }
    private void invalidate(State state) {
        synchronized (state) {
            state.closed = true;
            if (state.active != null) terminate(state.active, "CANCELLED", "scope_changed");
            synchronized (operations) {
                for (OperationKey key : state.operations) { var binding = operations.get(key); if (binding != null && binding.state == state) { binding.state = null; binding.task = null; } }
                state.operations.clear();
            }
            state.history.clear(); state.toolHistory.clear(); state.runs.clear(); state.tasks.clear(); sessions.remove(state.view.sessionId(), state);
        }
    }
    public void close(String id) { if (sessions.containsKey(id)) invalidate(owned(id)); else identity(); }
    @Override public void authorize(AiExecutionContext context) {
        State state = sessions.get(context.scope().reference());
        if (state == null || !Objects.equals(context.scope().permissionVersion(), state.view.contextVersion())
                || !Objects.equals(context.actorId(), state.owner.actor()) || !Objects.equals(context.organizationId(), state.owner.organization())
                || !Objects.equals(context.proxyOrganizationId(), state.owner.proxy())) throw AiErrors.error("forbidden");
        try { authorized(state.owner, () -> { check(state); return null; }); }
        catch (RuntimeException failure) { invalidate(state); throw failure; }
    }
    @Override public String auditReference(AiExecutionContext context) {
        var state = sessions.get(context.scope().reference());
        if (state == null) throw AiErrors.error("scope_changed");
        return state.view.scene() + ":" + Objects.toString(state.context.resourceId(), "") + "@" + state.context.revision()
                + (state.context.selectedTables() == null ? "" : ":tables=" + state.context.selectedTables());
    }
    @Override public void authorizeInput(AiExecutionContext context, Prompt prompt) { authorize(context); }
    private void check(State state) {
        if (state.closed || System.currentTimeMillis() >= state.view.expiresAt()) throw AiErrors.error("scope_changed");
        try {
            access.verify(state.owner, false);
            if (!same(access.capture(), state.owner)) throw AiErrors.error("forbidden");
            if (!Objects.equals(state.usageRevision, runtime.getObject().usageRevision(state.owner))) throw AiErrors.error("scope_changed");
            authorized(state.owner, () -> { scene(state.view.scene()).authorize(state.context); return null; });
            if (!Objects.equals(state.usageRevision, runtime.getObject().usageRevision(state.owner))) throw AiErrors.error("scope_changed");
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
        var identity = access.capture(); access.verify(identity, false); runtime.getObject().verifyUsage(identity); return identity;
    }
    private boolean same(AiAccess.Identity a, AiAccess.Identity b) { return Objects.equals(a, b); }
    private AiConversationScene scene(String code) {
        var scene = code == null ? null : scenes.get(code); if (scene == null || scene.backgroundOnly()) throw AiErrors.error("unsupported"); return scene;
    }
    public void policyDisabled(Long organization) {
        sessions.values().forEach(state -> {
            if (organization == null || organization.equals(state.owner.organization())) {
                synchronized (state) { invalidate(state); }
            }
        });
    }
    private void expire() {
        long now = System.currentTimeMillis();
        operations.entrySet().removeIf(entry -> entry.getValue().expiresAt <= now);
        sessions.forEach((id, state) -> {
            synchronized (state) {
                try { authorized(state.owner, () -> { runtime.getObject().verifyUsage(state.owner); check(state); return null; }); }
                catch (AiErrors.Failure revoked) { invalidate(state); return; }
                catch (RuntimeException unavailable) { return; }
                if (state.view.expiresAt() <= now) invalidate(state);
                else if (state.active != null) {
                    Run run = state.active;
                    if (now >= run.stream.deadline()) terminate(run, "FAILED", "timeout");
                    else if (now >= run.lease) invalidate(state);
                }
            }
        });
    }
    @PreDestroy public void destroy() { expiry.shutdownNow(); sessions.values().forEach(this::invalidate); sessions.clear(); operations.clear(); }
}
