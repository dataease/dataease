package io.dataease.ai.manage;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.ai.dao.entity.*;
import io.dataease.ai.support.AiErrors;
import io.dataease.ai.support.AiToolProtocol;
import io.dataease.api.ai.runtime.*;
import io.dataease.api.ai.conversation.AiConversationDtos.Tool;
import io.dataease.api.ai.conversation.AiConversationDtos.Result;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import io.dataease.api.ai.task.AiTaskDtos.*;
import io.dataease.utils.IDUtils;
import jakarta.persistence.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

/** Durable owner of independent background assistance. All database transactions are short. */
@Component
public class AiTaskManage implements AiToolConversationFeature {
    public static final long DELIVERY_MILLIS = TimeUnit.DAYS.toMillis(7);
    private static final long NODE_LEASE_MILLIS = 15000;
    private final String nodeId = UUID.randomUUID().toString();
    private final AtomicBoolean started = new AtomicBoolean();
    private volatile boolean stopped;
    private final ScheduledExecutorService heartbeat = Executors.newSingleThreadScheduledExecutor(r -> daemon(r, "de-ai-task-heartbeat"));
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> daemon(r, "de-ai-task-dispatch"));
    private final Map<Long, Run> running = new ConcurrentHashMap<>();
    private record Work(Long taskId, Long attemptId, String fence, AiAccess.Identity owner, Submit input, AiConversationScene.Context context, long deadline) { }
    private static final class Run {
        final Work work;
        volatile AiRuntimeManage.StreamHandle stream;
        final StringBuilder text = new StringBuilder();
        final List<AiToolProtocol.Message> transcript = new ArrayList<>();
        final List<Tool> tools = new ArrayList<>();
        final Set<String> ids = new HashSet<>(), requests = new HashSet<>();
        int toolSteps;
        final Set<String> invalidRequests = new HashSet<>();
        volatile String failureCode;
        volatile boolean replayUnsafe;
        String recoverySource = "LEGACY";
        Run(Work work) { this.work = work; }
    }
    private static Thread daemon(Runnable work, String name) { var thread = new Thread(work, name); thread.setDaemon(true); return thread; }
    private final EntityManager em;
    private final TransactionTemplate tx;
    private final Map<String, AiConversationScene> scenes;
    private final ObjectProvider<AiRuntimeManage> runtime;
    private final AiAccess access;
    private final ObjectMapper json;
    private final ObjectProvider<AiConversationManage> conversations;
    public AiTaskManage(EntityManager em, PlatformTransactionManager manager, ObjectProvider<AiConversationScene> scenes,
                        ObjectProvider<AiRuntimeManage> runtime, AiAccess access, ObjectMapper json) {
        this(em, manager, scenes, runtime, access, json, null);
    }
    @org.springframework.beans.factory.annotation.Autowired
    public AiTaskManage(EntityManager em, PlatformTransactionManager manager, ObjectProvider<AiConversationScene> scenes,
                        ObjectProvider<AiRuntimeManage> runtime, AiAccess access, ObjectMapper json, ObjectProvider<AiConversationManage> conversations) {
        this.conversations = conversations;
        this.em = em; tx = new TransactionTemplate(manager); this.runtime = runtime; this.access = access; this.json = json;
        this.scenes = scenes.stream().collect(Collectors.toUnmodifiableMap(AiConversationScene::code, scene -> scene));
    }
    public Task submit(Submit request) {
        var owner = identity();
        if (request == null || !key(request.submissionKey()) || request.editorId() == null || request.editorId().isBlank() || request.editorId().length() > 100
                || request.text() == null || request.text().isBlank() || request.text().length() > 30000
                || request.sql() != null && request.sql().length() > 30000
                || request.resourceId() != null && !request.resourceId().matches("[0-9]{1,20}|")) throw AiErrors.error("invalid");
        var scene = scene(request.scene());
        if (!scene.capability().equals(request.capability())) throw AiErrors.error("unsupported");
        String binding = AiAccess.binding(owner), submission = hash(binding + ":" + request.submissionKey());
        // Keep the exact V1 seven-field normalization for already persisted idempotency fingerprints.
        String legacy = encode(new LegacyInput(null, request.capability(), request.scene(), request.resourceId(), request.editorId(), request.sql(), request.text()));
        String normalized = request.origin() == null ? legacy : legacy + encode(request.origin());
        String fingerprint = hash(request.selectedTables() == null || request.selectedTables().isEmpty() ? normalized : normalized + encode(request.selectedTables()));
        String input = encode(new Submit(null, request.capability(), request.scene(), request.resourceId(), request.editorId(), request.sql(), request.text(), request.origin(), request.selectedTables()));
        var existing = bySubmission(submission);
        if (existing != null) return repeated(existing, fingerprint, owner);
        var context = authorized(owner, () -> {
            var resolved = scene.resolveTask(Objects.toString(request.resourceId(), ""), Objects.toString(request.sql(), ""), request.selectedTables());
            scene.authorize(resolved); return resolved;
        });
        if (request.origin() != null) conversationManage().validateTaskOrigin(request.origin(), request, context);
        var claims = authorized(owner, () -> resourceKeys(scene, context));
        runtime.getObject().verifyTaskAdmission(owner);
        try {
            Task accepted = tx.execute(status -> {
                // Independent shared acceptance coordinator; no external I/O under this lock.
                coordinate();
                var duplicate = bySubmission(submission);
                if (duplicate != null) {
                    if (!fingerprint.equals(duplicate.getInputHash())) throw AiErrors.error("conflict");
                    return view(duplicate);
                }
                resourceConflict(claims, binding);
                admission(owner.actor());
                var task = new CoreAiTask(); task.setId(IDUtils.snowID()); task.setOwnerBinding(binding); task.setActorId(owner.actor());
                task.setSubmissionHash(submission); task.setInputHash(fingerprint); task.setInputJson(input); task.setOwnerJson(encode(owner));
                task.setContextJson(encode(context)); task.setCreatedAt(System.currentTimeMillis());
                var attempt = attempt(task, 1, hash(task.getId() + ":initial"));
                task.setCurrentAttemptId(attempt.getId()); em.persist(task); em.persist(attempt); claimResources(claims, task.getId(), attempt.getId()); em.flush(); return view(task);
            });
            return checkedView(em.find(CoreAiTask.class, accepted.taskId()), owner);
        } catch (RuntimeException failure) {
            // A unique constraint settles duplicate requests even across independent nodes.
            existing = bySubmission(submission);
            if (existing != null) return repeated(existing, fingerprint, owner);
            throw safeResourceFailure(failure, owner);
        }
    }
    private RuntimeException safeResourceFailure(RuntimeException failure, AiAccess.Identity owner) {
        if (failure instanceof AiErrors.Failure busy && "task_resource_busy".equals(busy.safeCode())
                && busy.getErrorData() instanceof Map<?, ?> details && details.get("taskId") instanceof String id) {
            try {
                var current = checkedView(em.find(CoreAiTask.class, Long.valueOf(id)), owner);
                if (!Set.of("QUEUED", "RUNNING").contains(current.status())) busy.setErrorData(null);
            } catch (RuntimeException unavailable) { busy.setErrorData(null); }
        }
        return failure;
    }
    public List<String> tables(String resourceId) {
        var owner = identity();
        var scene = scene("structure-mutex");
        if (!(scene instanceof AiStructureMutexScene structure)) throw AiErrors.error("unsupported");
        return authorized(owner, () -> structure.tables(resourceId));
    }
    private List<String> resourceKeys(AiConversationScene scene, AiConversationScene.Context context) {
        var declarations = scene.resourceClaims(context);
        if (declarations == null || declarations.size() > 32) throw AiErrors.error("invalid");
        var keys = new TreeSet<String>();
        for (var declaration : declarations) {
            if (declaration == null || declaration.datasourceId() == null || declaration.table() == null || declaration.operation() == null
                    || declaration.table().isBlank() || declaration.operation().isBlank()) throw AiErrors.error("invalid");
            keys.add(hash(encode(declaration)));
        }
        return List.copyOf(keys);
    }
    private void resourceConflict(List<String> keys, String ownerBinding) {
        if (!keys.isEmpty()) expireWaiting();
        for (var key : keys) {
            var claim = em.find(CoreAiTaskResource.class, key, LockModeType.PESSIMISTIC_WRITE);
            if (claim == null) continue;
            var failure = AiErrors.error("task_resource_busy");
            var owner = em.find(CoreAiTask.class, claim.getTaskId());
            if (owner != null && ownerBinding.equals(owner.getOwnerBinding()) && Objects.equals(owner.getCurrentAttemptId(), claim.getAttemptId()))
                ((io.dataease.exception.DEException) failure).setErrorData(Map.of("taskId", owner.getId().toString()));
            throw failure;
        }
    }
    private void claimResources(List<String> keys, Long task, Long attempt) {
        for (var key : keys) {
            var claim = new CoreAiTaskResource(); claim.setResourceKey(key); claim.setTaskId(task); claim.setAttemptId(attempt); em.persist(claim);
        }
    }
    private CoreAiTaskSchedule coordinate() {
        var coordinator = em.find(CoreAiTaskSchedule.class, 1L, LockModeType.PESSIMISTIC_WRITE);
        if (coordinator == null) throw AiErrors.error("configuration_unavailable");
        return coordinator;
    }
    private void admission(Long actor) {
        expireWaiting();
        var settings = em.find(CoreAiSetting.class, 1L);
        Long total = em.createQuery("select count(a) from CoreAiTaskAttempt a where a.status='QUEUED'", Long.class).getSingleResult();
        Long own = em.createQuery("select count(t) from CoreAiTask t, CoreAiTaskAttempt a where t.currentAttemptId=a.id and t.actorId=:actor and a.status='QUEUED'", Long.class)
                .setParameter("actor", actor).getSingleResult();
        if (total >= settings.getTaskMaxQueued()) throw AiErrors.error("task_queue_full");
        if (own >= settings.getTaskMaxQueuedPerUser()) throw AiErrors.error("task_user_queue_full");
    }
    private CoreAiTaskAttempt attempt(CoreAiTask task, int number, String retryHash) {
        var attempt = new CoreAiTaskAttempt(); attempt.setId(IDUtils.snowID()); attempt.setTaskId(task.getId());
        attempt.setAttemptNumber(number); attempt.setRetryHash(retryHash); attempt.setStatus("QUEUED"); attempt.setCreatedAt(System.currentTimeMillis());
        var coordinator = em.find(CoreAiTaskSchedule.class, 1L);
        coordinator.setLastSequence(Math.addExact(coordinator.getLastSequence(), 1L)); attempt.setQueueSequence(coordinator.getLastSequence());
        var settings = em.find(CoreAiSetting.class, 1L); em.refresh(settings);
        attempt.setQueueDeadline(attempt.getCreatedAt() + settings.getTaskMaxWaitSeconds() * 1000L);
        attempt.setRecoveryJson(encode(new RecoverySnapshot(new io.dataease.api.ai.config.AiConfigDtos.Recovery(settings.getRecoveryStepRetries(),
                settings.getRecoveryAttemptRetries(), settings.getRecoveryInitialDelayMillis()), null, "ACCEPTED")));
        return attempt;
    }
    public List<Task> active() {
        var owner = identity();
        var rows = em.createQuery("select t from CoreAiTask t, CoreAiTaskAttempt a where t.currentAttemptId=a.id and t.ownerBinding=:owner and a.status in ('QUEUED','RUNNING') order by t.createdAt", CoreAiTask.class)
                .setParameter("owner", AiAccess.binding(owner)).setMaxResults(10020).getResultList();
        return rows.stream().map(task -> checkedView(task, owner)).toList();
    }
    /** Pending delivery only, never a task history or inbox. Re-authorize outside metadata transactions. */
    public List<Notice> notices() {
        var owner = identity();
        var result = new ArrayList<Notice>();
        int offset = 0;
        while (result.size() < 50) {
            var rows = em.createQuery("select a from CoreAiTaskAttempt a, CoreAiTask t where a.taskId=t.id and t.ownerBinding=:owner and a.status in ('COMPLETED','FAILED','CANCELLED','INTERRUPTED') and a.finishedAt is not null and a.deliveryPurgedAt is null and a.noticeAckAt is null and a.deliveryExpiresAt>:now order by a.finishedAt,a.id", CoreAiTaskAttempt.class)
                    .setParameter("owner", AiAccess.binding(owner)).setParameter("now", System.currentTimeMillis()).setFirstResult(offset).setMaxResults(50).getResultList();
            if (rows.isEmpty()) break;
            offset += rows.size();
            for (var attempt : rows) {
                var task = em.find(CoreAiTask.class, attempt.getTaskId());
                try {
                    authorized(owner, () -> { scene(input(task).scene()).authorize(context(task)); return null; });
                    if (attempt.getDeliveryExpiresAt() > System.currentTimeMillis())
                        result.add(new Notice(task.getId(), attempt.getId(), attempt.getAttemptNumber(), attempt.getStatus(), attempt.getErrorCode(), attempt.getDeliveryExpiresAt(), retryEligible(attempt)));
                } catch (AiErrors.Failure denied) { /* A revoked resource is never disclosed by its old notice. */ }
                if (result.size() == 50) break;
            }
            if (rows.size() < 50) break;
        }
        runtime.getObject().verifyUsage(owner);
        return result;
    }
    public void acknowledge(Long attemptId) {
        var owner = identity();
        var attempt = attemptId == null ? null : em.find(CoreAiTaskAttempt.class, attemptId);
        if (attempt == null) throw AiErrors.error("forbidden");
        checkedView(owned(attempt.getTaskId()), owner);
        tx.executeWithoutResult(status -> em.createQuery("update CoreAiTaskAttempt a set a.noticeAckAt=:now where a.id=:id and a.status in ('COMPLETED','FAILED','CANCELLED','INTERRUPTED') and a.finishedAt is not null and a.deliveryPurgedAt is null and a.noticeAckAt is null and a.deliveryExpiresAt>:now")
                .setParameter("now", System.currentTimeMillis()).setParameter("id", attemptId).executeUpdate());
    }
    void ended(CoreAiTaskAttempt attempt, String state, String code, String result) {
        long now = System.currentTimeMillis();
        attempt.setStatus(state); attempt.setErrorCode(code); attempt.setResultJson(result); attempt.setFinishedAt(now);
        attempt.setDeliveryExpiresAt(now + TimeUnit.DAYS.toMillis(em.find(CoreAiSetting.class, 1L).getDeliveryRetentionDays())); attempt.setNoticeAckAt(null);
        attempt.setFence(null); attempt.setNodeId(null);
        // Persist the durable fence before releasing the exact owner, in this same terminal transaction.
        em.flush();
        em.createQuery("delete from CoreAiTaskResource r where r.taskId=:task and r.attemptId=:attempt")
                .setParameter("task", attempt.getTaskId()).setParameter("attempt", attempt.getId()).executeUpdate();
    }
    private void interrupted(String node) {
        tx.executeWithoutResult(status -> {
            coordinate(); expireWaiting();
            var rows = em.createQuery("select a from CoreAiTaskAttempt a where a.status='RUNNING' and " +
                    (node == null ? "a.nodeId in (select n.id from CoreAiTaskNode n where n.heartbeatAt<=:cutoff)" : "a.nodeId=:node"), CoreAiTaskAttempt.class);
            if (node == null) rows.setParameter("cutoff", System.currentTimeMillis() - NODE_LEASE_MILLIS); else rows.setParameter("node", node);
            for (var candidate : rows.getResultList()) {
                em.find(CoreAiTask.class, candidate.getTaskId(), LockModeType.PESSIMISTIC_WRITE);
                var attempt = em.find(CoreAiTaskAttempt.class, candidate.getId(), LockModeType.PESSIMISTIC_WRITE);
                em.refresh(attempt);
                if ("RUNNING".equals(attempt.getStatus())) ended(attempt, "INTERRUPTED", "interrupted", null);
            }
        });
    }
    private record LegacyInput(String submissionKey, String capability, String scene, String resourceId, String editorId, String sql, String text) { }
    private AiConversationManage conversationManage() {
        if (conversations == null || conversations.getIfAvailable() == null) throw AiErrors.error("scope_changed");
        return conversations.getObject();
    }
    /** Reads its own immutable task association, never the pending-notice queue. */
    public Task conversation(Long id, Origin origin) {
        var task = owned(id); deliverable(em.find(CoreAiTaskAttempt.class, task.getCurrentAttemptId())); var input = input(task);
        if (origin == null || !origin.equals(input.origin())) throw AiErrors.error("scope_changed");
        var result = checkedView(task, identity());
        if (!"COMPLETED".equals(result.status()) || result.result() == null) throw AiErrors.error("conflict");
        conversationManage().appendTaskResult(origin, input, context(task), result.attemptId().toString(), result.result());
        return result;
    }
    public Task get(Long id) { return checkedView(owned(id), identity()); }
    public Task cancel(Long id) {
        var task = owned(id);
        return tx.execute(status -> {
            coordinate();
            var locked = em.find(CoreAiTask.class, task.getId(), LockModeType.PESSIMISTIC_WRITE);
            var attempt = em.find(CoreAiTaskAttempt.class, locked.getCurrentAttemptId(), LockModeType.PESSIMISTIC_WRITE);
            if (Set.of("QUEUED", "RUNNING").contains(attempt.getStatus())) {
                ended(attempt, "CANCELLED", "cancelled", null); em.flush();
            }
            // Cancellation is a cleanup operation; it never delivers a protected resource/result.
            return new Task(locked.getId(), attempt.getId(), attempt.getAttemptNumber(), attempt.getStatus(), null, null, null,
                    locked.getCreatedAt(), attempt.getStartedAt(), attempt.getFinishedAt(), attempt.getErrorCode(), null, null, null, null,
                    attempt.getExecutionId(), attempt.getSourceAttemptId(), retryEligible(attempt), null);
        });
    }
    public Task retry(Long id, Retry request) {
        var task = owned(id); var owner = identity();
        checkedView(task, owner);
        if (request == null || !key(request.retryKey()) || request.expectedAttemptId() == null) throw AiErrors.error("invalid");
        String retry = hash(task.getId() + ":" + request.retryKey());
        var repeated = em.createQuery("select a from CoreAiTaskAttempt a where a.retryHash=:hash", CoreAiTaskAttempt.class)
                .setParameter("hash", retry).setMaxResults(1).getResultList();
        if (!repeated.isEmpty()) {
            if (!Objects.equals(repeated.get(0).getSourceAttemptId(), request.expectedAttemptId())) throw AiErrors.error("conflict");
            return authorized(owner, () -> { scene(input(task).scene()).authorize(context(task)); return view(task, repeated.get(0)); });
        }
        var claims = authorized(owner, () -> resourceKeys(scene(input(task).scene()), context(task)));
        runtime.getObject().verifyTaskAdmission(owner);
        Task accepted;
        try {
            accepted = tx.execute(status -> {
                coordinate();
                var locked = em.find(CoreAiTask.class, task.getId(), LockModeType.PESSIMISTIC_WRITE);
                var duplicate = em.createQuery("select a from CoreAiTaskAttempt a where a.retryHash=:hash", CoreAiTaskAttempt.class)
                        .setParameter("hash", retry).setMaxResults(1).getResultList();
                if (!duplicate.isEmpty()) {
                    if (!Objects.equals(duplicate.get(0).getSourceAttemptId(), request.expectedAttemptId())) throw AiErrors.error("conflict");
                    return view(locked, duplicate.get(0));
                }
                var previous = em.find(CoreAiTaskAttempt.class, locked.getCurrentAttemptId());
                if (!Objects.equals(previous.getId(), request.expectedAttemptId()) || !Set.of("FAILED", "CANCELLED", "INTERRUPTED").contains(previous.getStatus()))
                    throw AiErrors.error("conflict");
                if (!Boolean.TRUE.equals(previous.getRetrySafe())) throw AiErrors.error("outcome_unknown");
                resourceConflict(claims, AiAccess.binding(owner));
                admission(owner.actor());
                var next = attempt(locked, previous.getAttemptNumber() + 1, retry); next.setSourceAttemptId(previous.getId()); em.persist(next);
                locked.setCurrentAttemptId(next.getId()); claimResources(claims, locked.getId(), next.getId()); em.flush(); return view(locked, next);
            });
        } catch (RuntimeException failure) { throw safeResourceFailure(failure, owner); }
        // A repeated retry may already have a result; recheck delivery after releasing all database locks.
        return authorized(owner, () -> { scene(input(task).scene()).authorize(context(task)); return accepted; });
    }
    private Task repeated(CoreAiTask task, String fingerprint, AiAccess.Identity owner) {
        if (!fingerprint.equals(task.getInputHash())) throw AiErrors.error("conflict");
        return checkedView(task, owner);
    }
    private Task checkedView(CoreAiTask task, AiAccess.Identity owner) {
        if (!AiAccess.binding(owner).equals(task.getOwnerBinding())) throw AiErrors.error("forbidden");
        deliverable(em.find(CoreAiTaskAttempt.class, task.getCurrentAttemptId()));
        return authorized(owner, () -> { runtime.getObject().verifyUsage(owner); scene(input(task).scene()).authorize(context(task)); var result = view(task); runtime.getObject().verifyUsage(owner); return result; });
    }
    private Task view(CoreAiTask task) {
        return view(task, em.find(CoreAiTaskAttempt.class, task.getCurrentAttemptId()));
    }
    private boolean retryEligible(CoreAiTaskAttempt attempt) {
        return Boolean.TRUE.equals(attempt.getRetrySafe()) && Set.of("FAILED", "CANCELLED", "INTERRUPTED").contains(attempt.getStatus())
                && attempt.getFinishedAt() != null && attempt.getDeliveryPurgedAt() == null && System.currentTimeMillis() < expires(attempt);
    }
    private Task view(CoreAiTask task, CoreAiTaskAttempt attempt) {
        deliverable(attempt);
        var input = input(task); var context = context(task);
        var projection = new Task(task.getId(), attempt.getId(), attempt.getAttemptNumber(), attempt.getStatus(), input.scene(), context.resourceId(), context.resourceName(),
                task.getCreatedAt(), attempt.getStartedAt(), attempt.getFinishedAt(), attempt.getErrorCode(), attempt.getModelId(), attempt.getModelRevision(), attempt.getModelIdentifier(),
                attempt.getResultJson() == null ? null : decode(attempt.getResultJson(), io.dataease.api.ai.conversation.AiConversationDtos.Result.class),
                attempt.getExecutionId(), attempt.getSourceAttemptId(), retryEligible(attempt),
                attempt.getRecoveryJson() == null ? null : decode(attempt.getRecoveryJson(), RecoverySnapshot.class).progress());
        deliverable(attempt); return projection;
    }
    private void deliverable(CoreAiTaskAttempt attempt) {
        if (attempt == null || !Set.of("QUEUED", "RUNNING").contains(attempt.getStatus())
                && (attempt.getFinishedAt() == null || attempt.getDeliveryPurgedAt() != null || System.currentTimeMillis() >= expires(attempt)))
            throw AiErrors.error("result_expired");
    }
    private long expires(CoreAiTaskAttempt attempt) {
        return attempt.getDeliveryExpiresAt() == null ? attempt.getFinishedAt() + DELIVERY_MILLIS : attempt.getDeliveryExpiresAt();
    }
    private CoreAiTask owned(Long id) {
        var owner = identity(); var task = id == null ? null : em.find(CoreAiTask.class, id);
        if (task == null || !task.getOwnerBinding().equals(AiAccess.binding(owner))) throw AiErrors.error("forbidden"); return task;
    }
    private CoreAiTask bySubmission(String hash) {
        return em.createQuery("select t from CoreAiTask t where t.submissionHash=:hash", CoreAiTask.class).setParameter("hash", hash).setMaxResults(1).getResultList().stream().findFirst().orElse(null);
    }
    private AiAccess.Identity identity() {
        if (RequestContextHolder.getRequestAttributes() == null) throw AiErrors.error("forbidden");
        var owner = access.capture(); runtime.getObject().verifyUsage(owner); return owner;
    }
    private <T> T authorized(AiAccess.Identity owner, java.util.function.Supplier<T> operation) { return new AiIdentityScope(access).call(owner, operation); }
    private Submit input(CoreAiTask task) { return decode(task.getInputJson(), Submit.class); }
    private AiConversationScene.Context context(CoreAiTask task) { return decode(task.getContextJson(), AiConversationScene.Context.class); }
    private AiConversationScene scene(String code) { var scene = scenes.get(code); if (scene == null) throw AiErrors.error("unsupported"); return scene; }
    private boolean key(String value) { return value != null && value.matches("[A-Za-z0-9_-]{1,100}"); }
    private String encode(Object value) { try { return json.writeValueAsString(value); } catch (Exception failure) { throw AiErrors.error("invalid"); } }
    private <T> T decode(String value, Class<T> type) { try { return json.readValue(value, type); } catch (Exception failure) { throw AiErrors.error("scope_changed"); } }
    private String hash(String value) { try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); } catch (Exception failure) { throw new IllegalStateException(failure); } }
    @Override public String code() { return "background-assistance"; }
    @Override public boolean supportsStreaming() { return true; }
    @Override public TaskIdentity taskIdentity(String taskId) {
        var task = task(taskId); var attempt = em.find(CoreAiTaskAttempt.class, task.getCurrentAttemptId());
        live(attempt);
        var owner = decode(task.getOwnerJson(), AiAccess.Identity.class);
        access.verify(owner, false);
        return new TaskIdentity(owner.actor(), owner.organization(), owner.proxy(), attempt.getId() + ":" + attempt.getFence());
    }
    @Override public void authorize(AiExecutionContext execution) {
        if (!execution.background()) throw AiErrors.error("forbidden");
        var task = task(execution.scope().reference()); var attempt = em.find(CoreAiTaskAttempt.class, task.getCurrentAttemptId());
        live(attempt);
        var owner = decode(task.getOwnerJson(), AiAccess.Identity.class);
        if (!Objects.equals(execution.scope().permissionVersion(), attempt.getId() + ":" + attempt.getFence())
                || !Objects.equals(owner.actor(), execution.actorId()) || !Objects.equals(owner.organization(), execution.organizationId())
                || !Objects.equals(owner.proxy(), execution.proxyOrganizationId())) throw AiErrors.error("forbidden");
        authorized(owner, () -> { scene(input(task).scene()).authorize(context(task)); return null; });
    }
    @Override public String auditReference(AiExecutionContext execution) {
        var task = task(execution.scope().reference()); var resource = context(task);
        return input(task).scene() + ":" + Objects.toString(resource.resourceId(), "") + "@" + resource.revision()
                + (resource.selectedTables() == null ? "" : ":tables=" + resource.selectedTables());
    }
    @Override public void authorizeInput(AiExecutionContext context, Prompt prompt) { authorize(context); }
    @Override public void authorizeToolInput(AiExecutionContext execution, AiToolProtocol.Input input) {
        authorize(execution); var task = task(execution.scope().reference());
        if (!scene(input(task).scene()).tools(context(task)).equals(input.tools())) throw AiErrors.error("scope_changed");
    }
    private CoreAiTask task(String id) {
        try { var task = em.find(CoreAiTask.class, Long.valueOf(id)); if (task != null) return task; }
        catch (NumberFormatException ignored) { }
        throw AiErrors.error("forbidden");
    }
    private void live(CoreAiTaskAttempt attempt) {
        if (attempt == null || !"RUNNING".equals(attempt.getStatus()) || attempt.getFence() == null || attempt.getNodeId() == null) throw AiErrors.error("cancelled");
        var node = em.find(CoreAiTaskNode.class, attempt.getNodeId());
        if (node == null || node.getHeartbeatAt() <= System.currentTimeMillis() - NODE_LEASE_MILLIS) throw AiErrors.error("cancelled");
    }
    /** Starts only after versioned migrations and all application runners have completed. */
    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        if (!started.compareAndSet(false, true)) return;
        tx.executeWithoutResult(status -> {
            var node = new CoreAiTaskNode(); node.setId(nodeId); node.setHeartbeatAt(System.currentTimeMillis()); em.persist(node);
        });
        heartbeat.scheduleWithFixedDelay(this::pulse, 1000, 1000, TimeUnit.MILLISECONDS);
        scheduler.scheduleWithFixedDelay(this::dispatch, 0, 250, TimeUnit.MILLISECONDS);
    }
    private void pulse() {
        if (stopped) return;
        try {
            boolean alive = Boolean.TRUE.equals(tx.execute(status -> em.createQuery("update CoreAiTaskNode n set n.heartbeatAt=:now where n.id=:id and n.heartbeatAt>:cutoff")
                    .setParameter("now", System.currentTimeMillis()).setParameter("id", nodeId).setParameter("cutoff", System.currentTimeMillis() - NODE_LEASE_MILLIS).executeUpdate() == 1));
            if (!alive) { stopped = true; interruptOwn(); running.values().forEach(this::close); running.clear(); }
        } catch (RuntimeException unavailable) { /* Lease expires closed; every execution boundary checks durable ownership. */ }
    }
    private void dispatch() {
        if (stopped) return;
        try {
            invalidateUnauthorized();
            interrupted(null);
            for (Run run : running.values()) {
                if (run.failureCode != null) { fail(run, AiErrors.error(run.failureCode)); continue; }
                try { check(run); if (run.stream != null) run.stream.verifyDelivery(); }
                catch (RuntimeException terminal) { fail(run, terminal); }
            }
            while (!stopped && running.size() < 20) {
                Work work = claim(); if (work == null) break;
                var run = new Run(work); running.put(work.attemptId(), run);
                try { execute(run); } catch (RuntimeException failure) { fail(run, failure); }
            }
        } catch (RuntimeException unavailable) { /* Durable queue survives; no model submission until database recovery. */ }
    }
    /** Called under the configuration transaction/coordinator: fence first, release exact resources second. */
    public void policyDisabled(Long organization, String reason) {
        if (!org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()) throw new IllegalStateException("AI policy invalidation requires transaction");
        coordinate();
        var revoked = new HashSet<Long>();
        for (var attempt : em.createQuery("select a from CoreAiTaskAttempt a, CoreAiTask t where t.currentAttemptId=a.id and a.status in ('QUEUED','RUNNING')", CoreAiTaskAttempt.class).getResultList()) {
            var task = em.find(CoreAiTask.class, attempt.getTaskId(), LockModeType.PESSIMISTIC_WRITE);
            if (organization != null && !organization.equals(decode(task.getOwnerJson(), AiAccess.Identity.class).organization())) continue;
            em.lock(attempt, LockModeType.PESSIMISTIC_WRITE);
            ended(attempt, "CANCELLED", reason, null); revoked.add(attempt.getId());
        }
        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(new org.springframework.transaction.support.TransactionSynchronization() {
            @Override public void afterCommit() {
                for (Long id : revoked) {
                    Run run = running.remove(id);
                    if (run != null) { if (run.stream != null) run.stream.finishAudit("CANCELLED"); close(run); }
                }
            }
        });
    }
    /** No business authorization or external metadata read holds a task/database lock. Capacity does not skip revocation. */
    private void invalidateUnauthorized() {
        boolean capacityFull = em.createQuery("select count(a) from CoreAiTaskAttempt a where a.status='RUNNING'", Long.class).getSingleResult()
                >= em.find(CoreAiSetting.class, 1L).getTaskMaxRunning();
        var candidates = em.createQuery("select t from CoreAiTask t, CoreAiTaskAttempt a where t.currentAttemptId=a.id and a.status in ('QUEUED','RUNNING') order by t.createdAt", CoreAiTask.class).setMaxResults(10020).getResultList();
        for (var task : candidates) {
            var attempt = em.find(CoreAiTaskAttempt.class, task.getCurrentAttemptId());
            try {
                var owner = decode(task.getOwnerJson(), AiAccess.Identity.class);
                if ("QUEUED".equals(attempt.getStatus())) runtime.getObject().verifyTaskAdmission(owner);
                else runtime.getObject().verifyTaskExecution(owner, attempt.getModelId(), attempt.getModelRevision(), attempt.getCredentialRevision());
                // Runnable work performs its initial resource read only after claim fixes the execution deadline.
                if ("QUEUED".equals(attempt.getStatus()) ? capacityFull : attempt.getModelId() != null)
                    authorized(owner, () -> { scene(input(task).scene()).authorize(context(task)); return null; });
            } catch (AiErrors.Failure invalid) {
                if (!Set.of("disabled", "organization_disabled", "forbidden", "scope_changed", "conflict", "model_unavailable").contains(invalid.safeCode())) continue;
                Long expected = attempt.getId();
                tx.executeWithoutResult(status -> {
                    coordinate(); var locked = em.find(CoreAiTask.class, task.getId(), LockModeType.PESSIMISTIC_WRITE);
                    var current = em.find(CoreAiTaskAttempt.class, expected, LockModeType.PESSIMISTIC_WRITE); em.refresh(current);
                    if (Objects.equals(locked.getCurrentAttemptId(), expected) && Set.of("QUEUED", "RUNNING").contains(current.getStatus()))
                        ended(current, "CANCELLED", invalid.safeCode(), null);
                });
            }
        }
    }
    private Work claim() {
        return tx.execute(status -> {
            coordinate(); expireWaiting();
            if (em.createQuery("select count(a) from CoreAiTaskAttempt a where a.status='RUNNING'", Long.class).getSingleResult() >= em.find(CoreAiSetting.class, 1L).getTaskMaxRunning()) return null;
            var node = em.find(CoreAiTaskNode.class, nodeId);
            if (node == null || node.getHeartbeatAt() <= System.currentTimeMillis() - NODE_LEASE_MILLIS) return null;
            var coordination = em.find(CoreAiTaskSchedule.class, 1L);
            var actors = em.createQuery("select distinct t.actorId from CoreAiTask t, CoreAiTaskAttempt a where t.currentAttemptId=a.id and a.status='QUEUED' and t.actorId is not null order by t.actorId", Long.class).getResultList();
            if (actors.isEmpty()) return null;
            Long previous = coordination.getLastActorId();
            Long actor = actors.stream().filter(candidate -> previous == null || candidate > previous).findFirst().orElse(actors.get(0));
            Long id = em.createQuery("select a.id from CoreAiTaskAttempt a, CoreAiTask t where t.currentAttemptId=a.id and t.actorId=:actor and a.status='QUEUED' order by a.queueSequence,a.id", Long.class)
                    .setParameter("actor", actor).setMaxResults(1).getSingleResult();
            String fence = UUID.randomUUID().toString();
            long now = System.currentTimeMillis();
            long deadline = now + em.find(CoreAiSetting.class, 1L).getRequestTimeoutSeconds() * 1000L;
            int won = em.createQuery("update CoreAiTaskAttempt a set a.status='RUNNING', a.nodeId=:node, a.fence=:fence, a.startedAt=:now, a.deadline=:deadline where a.id=:id and a.status='QUEUED' and a.queueDeadline>:now")
                    .setParameter("node", nodeId).setParameter("fence", fence).setParameter("now", now).setParameter("deadline", deadline).setParameter("id", id).executeUpdate();
            if (won != 1) return null;
            coordination.setLastActorId(actor);
            var attempt = em.find(CoreAiTaskAttempt.class, id); var task = em.find(CoreAiTask.class, attempt.getTaskId());
            return new Work(task.getId(), id, fence, decode(task.getOwnerJson(), AiAccess.Identity.class), input(task), context(task), deadline);
        });
    }
    private void expireWaiting() {
        var expired = em.createQuery("select a from CoreAiTaskAttempt a, CoreAiTask t where t.currentAttemptId=a.id and a.status='QUEUED' and (a.queueDeadline is null or a.queueDeadline<=:now or t.actorId is null)", CoreAiTaskAttempt.class)
                .setParameter("now", System.currentTimeMillis()).getResultList();
        for (var attempt : expired) {
            em.find(CoreAiTask.class, attempt.getTaskId(), LockModeType.PESSIMISTIC_WRITE);
            em.lock(attempt, LockModeType.PESSIMISTIC_WRITE);
            if ("QUEUED".equals(attempt.getStatus())) ended(attempt, "FAILED", "queue_timeout", null);
        }
    }
    /** Parser/validator hot loops only inspect local liveness; they never deliver or commit output. */
    private void progress(Run run) {
        if (stopped || run.failureCode != null || running.get(run.work.attemptId()) != run) throw AiErrors.error("cancelled");
        if (System.currentTimeMillis() >= run.work.deadline() || run.stream != null && System.currentTimeMillis() >= run.stream.deadline()) throw AiErrors.error("timeout");
    }
    /** Fresh durable ownership check at dispatch, model/tool and result boundaries. */
    private void active(Run run) {
        progress(run);
        var attempt = em.find(CoreAiTaskAttempt.class, run.work.attemptId()); live(attempt);
        if (!Objects.equals(run.work.fence(), attempt.getFence()) || !nodeId.equals(attempt.getNodeId())) throw AiErrors.error("cancelled");
    }
    private void check(Run run) {
        active(run); authorized(run.work.owner(), () -> { scene(run.work.input().scene()).authorize(run.work.context()); return null; });
    }
    private record RecoverySnapshot(io.dataease.api.ai.config.AiConfigDtos.Recovery policy, io.dataease.api.ai.conversation.AiConversationDtos.RecoveryProgress progress, String source) { }
    private void recoveryProgress(Run run) {
        var progress = run.stream.recovery().progress();
        String snapshot = encode(new RecoverySnapshot(run.stream.recovery().policy(), new io.dataease.api.ai.conversation.AiConversationDtos.RecoveryProgress(
                progress.stepId(), progress.stepKind(), progress.technicalRetries(), progress.corrections(), progress.reason()), run.recoverySource));
        boolean saved = Boolean.TRUE.equals(tx.execute(status -> em.createQuery("update CoreAiTaskAttempt a set a.recoveryJson=:progress where a.id=:id and a.status='RUNNING' and a.fence=:fence and a.nodeId=:node")
                .setParameter("progress", snapshot).setParameter("id", run.work.attemptId()).setParameter("fence", run.work.fence()).setParameter("node", nodeId).executeUpdate() == 1));
        if (!saved) throw AiErrors.error("cancelled");
    }
    private void execute(Run run) {
        check(run);
        var work = run.work; var scene = scene(work.input().scene());
        var definitions = scene.tools(work.context());
        String draft = AiPromptFingerprint.of(new Prompt(Objects.toString(work.input().sql(), "")));
        String version = work.attemptId() + ":" + work.fence();
        AiRuntimeManage.StreamHandle stream;
        AiToolProtocol.Input input = null;
        if (definitions.isEmpty()) {
            var prompt = new Prompt(List.of(new SystemMessage(work.context().instruction()), new UserMessage(work.input().text())));
            stream = runtime.getObject().streamTask(code(), work.taskId().toString(), new AiDataScope(work.taskId().toString(), version, AiPromptFingerprint.of(prompt), false, null), prompt, work.deadline());
        } else {
            run.transcript.add(AiToolProtocol.Message.text("system", work.context().instruction()));
            run.transcript.add(AiToolProtocol.Message.text("user", work.input().text())); input = new AiToolProtocol.Input(run.transcript, definitions);
            stream = runtime.getObject().streamTaskTools(code(), work.taskId().toString(), new AiDataScope(work.taskId().toString(), version, AiToolProtocol.fingerprint(input), false, null), input, work.deadline());
        }
        run.stream = stream;
        var attemptIdentity = em.find(CoreAiTaskAttempt.class, work.attemptId());
        var acceptedPolicy = attemptIdentity.getRecoveryJson() == null ? null : decode(attemptIdentity.getRecoveryJson(), RecoverySnapshot.class);
        // Legacy records never had an acceptance snapshot. Do not infer one from current admin settings.
        if (acceptedPolicy != null && "ACCEPTED".equals(acceptedPolicy.source())) {
            stream.recovery().freeze(acceptedPolicy.policy()); run.recoverySource = "ACCEPTED";
        } else stream.recovery().freeze(new io.dataease.api.ai.config.AiConfigDtos.Recovery(0, 0, 1000));
        stream.recovery().link(new io.dataease.api.ai.runtime.AiExecutionLink(work.taskId().toString(), work.attemptId().toString(), attemptIdentity.getAttemptNumber(),
                attemptIdentity.getSourceAttemptId() == null ? null : attemptIdentity.getSourceAttemptId().toString()));
        stream.recovery().observe(ignored -> recoveryProgress(run));
        var model = stream.model();
        // Pin actual selected model before any network send; a failed state commit cannot submit a request.
        boolean saved = Boolean.TRUE.equals(tx.execute(status -> em.createQuery("update CoreAiTaskAttempt a set a.modelId=:model, a.modelRevision=:revision, a.credentialRevision=:credential, a.modelIdentifier=:identifier, a.deadline=:deadline, a.executionId=:execution where a.id=:id and a.status='RUNNING' and a.fence=:fence and a.nodeId=:node")
                .setParameter("model", model.id()).setParameter("revision", model.revision()).setParameter("credential", model.credentialRevision()).setParameter("identifier", model.identifier())
                .setParameter("deadline", stream.deadline()).setParameter("execution", stream.executionId()).setParameter("id", work.attemptId()).setParameter("fence", work.fence()).setParameter("node", nodeId).executeUpdate() == 1));
        if (!saved) throw AiErrors.error("cancelled");
        if (stream instanceof AiRuntimeManage.ToolStreaming tools) tools.send(input, () -> progress(run), value -> append(run, value), step -> step(run, step), failure -> fail(run, failure));
        else ((AiRuntimeManage.Streaming) stream).start(() -> progress(run), value -> append(run, value), () -> complete(run, draft), failure -> fail(run, failure));
    }
    private void append(Run run, String value) {
        synchronized (run) {
            progress(run); if (run.text.length() + value.length() > 262144) throw AiErrors.error("output_limit"); run.text.append(value);
        }
    }
    private void step(Run run, AiToolProtocol.Step step) {
        try {
            check(run); var stream = (AiRuntimeManage.ToolStreaming) run.stream; stream.verifyStep();
            if (step.calls().isEmpty()) {
                complete(run, AiPromptFingerprint.of(new Prompt(Objects.toString(run.work.input().sql(), "")))); return;
            }
            if (step.calls().size() != 1 || run.toolSteps >= AiToolProtocol.MAX_MODEL_STEPS - 1) throw AiErrors.error("output_limit");
            var call = step.calls().get(0); var scene = scene(run.work.input().scene());
            var definition = scene.tools(run.work.context()).stream().filter(tool -> tool.name().equals(call.name())).findFirst().orElseThrow(() -> AiErrors.error("unsupported"));
            Map<String, String> arguments;
            try { arguments = AiToolProtocol.arguments(definition, call); }
            catch (AiErrors.Failure malformed) {
                if (!"invalid".equals(malformed.safeCode())) throw malformed;
                String feedback = AiToolProtocol.correction(call, stream.recovery(), run.invalidRequests); stream.result(feedback);
                run.transcript.add(AiToolProtocol.Message.assistant(step)); run.transcript.add(AiToolProtocol.Message.result(call, feedback));
                stream.send(new AiToolProtocol.Input(run.transcript, scene.tools(run.work.context())), () -> progress(run), value -> append(run, value), next -> step(run, next), failure -> fail(run, failure));
                return;
            }
            if (!run.ids.add(call.id()) || !run.requests.add(call.name() + ":" + new TreeMap<>(arguments))) throw AiErrors.error("output_limit");
            run.toolSteps++;
            java.util.function.Supplier<String> execute = () -> authorized(run.work.owner(), () -> {
                check(run); stream.verifyStep();
                String value = scene.execute(run.work.context(), call.name(), arguments, () -> progress(run), stream.deadline());
                check(run); stream.verifyStep(); return value;
            });
            if (definition.retrySafety() == AiToolProtocol.RetrySafety.UNKNOWN) {
                run.replayUnsafe = true;
                boolean safeSaved = Boolean.TRUE.equals(tx.execute(status -> em.createQuery("update CoreAiTaskAttempt a set a.retrySafe=false where a.id=:id and a.status='RUNNING' and a.fence=:fence and a.nodeId=:node")
                        .setParameter("id", run.work.attemptId()).setParameter("fence", run.work.fence()).setParameter("node", nodeId).executeUpdate() == 1));
                if (!safeSaved) throw AiErrors.error("cancelled");
            }
            String result = stream.recovery().execute("TOOL", stream.deadline(), () -> progress(run),
                    () -> definition.retrySafety() == AiToolProtocol.RetrySafety.UNKNOWN, () -> { }, () -> stream.recovery().tool(call.id(), call.name(), arguments, execute));
            active(run); stream.result(result);
            if (AiToolProtocol.invalidResult(result) && !stream.recovery().correct(call.name() + ":" + new TreeMap<>(arguments))) throw AiErrors.error("invalid");
            run.tools.add(new Tool(call.id(), call.name(), "SUCCEEDED", arguments, result));
            run.transcript.add(AiToolProtocol.Message.assistant(step)); run.transcript.add(AiToolProtocol.Message.result(call, result));
            stream.send(new AiToolProtocol.Input(run.transcript, scene.tools(run.work.context())), () -> progress(run), value -> append(run, value), next -> step(run, next), failure -> fail(run, failure));
        } catch (RuntimeException failure) { fail(run, failure); }
    }
    private void complete(Run run, String draft) {
        try {
            check(run); run.stream.verifyDelivery();
            if (run.text.toString().isBlank()) throw AiErrors.error("response_invalid");
            Result result = scene(run.work.input().scene()).result(run.work.context(), run.text.toString(), run.tools, run.work.input().editorId(), draft);
            String encoded = encode(result);
            if (encoded.getBytes(StandardCharsets.UTF_8).length > 1048576) throw AiErrors.error("output_limit");
            terminal(run, "COMPLETED", null, encoded);
        } catch (RuntimeException failure) { fail(run, failure); }
    }
    private void fail(Run run, Throwable failure) {
        String code = run.replayUnsafe ? "outcome_unknown" : failure instanceof AiErrors.Failure known ? known.safeCode() : "upstream_failed";
        try { terminal(run, Set.of("disabled", "organization_disabled", "forbidden", "scope_changed", "conflict", "model_unavailable").contains(code) ? "CANCELLED" : "FAILED", code, null); }
        catch (RuntimeException unavailable) {
            // Keep a bounded slot until a failure terminal can commit; never strand RUNNING under a healthy node.
            close(run); run.failureCode = code;
            if (!stopped) running.putIfAbsent(run.work.attemptId(), run);
        }
    }
    private void terminal(Run run, String state, String error, String result) {
        try {
            boolean saved = Boolean.TRUE.equals(tx.execute(status -> {
                // The lock orders terminal commit against cancellation/retry. DB ownership is the final arbiter.
                coordinate();
                em.find(CoreAiTask.class, run.work.taskId(), LockModeType.PESSIMISTIC_WRITE);
                var attempt = em.find(CoreAiTaskAttempt.class, run.work.attemptId(), LockModeType.PESSIMISTIC_WRITE);
                if (attempt == null || !"RUNNING".equals(attempt.getStatus()) || !Objects.equals(attempt.getFence(), run.work.fence()) || !nodeId.equals(attempt.getNodeId())) return false;
                live(attempt);
                ended(attempt, state, error, result); em.flush(); return true;
            }));
            if (saved && run.stream != null) run.stream.finishAudit(state);
        } finally { close(run); running.remove(run.work.attemptId(), run); }
    }
    private void close(Run run) { if (run.stream != null) run.stream.close(); }
    private void interruptOwn() {
        interrupted(nodeId);
    }
    @PreDestroy public void destroy() {
        stopped = true; scheduler.shutdownNow(); heartbeat.shutdownNow();
        if (started.get()) { try { interruptOwn(); } finally { running.values().forEach(this::close); running.clear(); } }
    }
}
