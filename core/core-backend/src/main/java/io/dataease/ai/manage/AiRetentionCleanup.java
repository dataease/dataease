package io.dataease.ai.manage;

import io.dataease.ai.dao.entity.*;
import jakarta.persistence.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Bounded repeatable sweeps. Each delivery or audit execution commits independently; failures retry next sweep. */
@Component
public class AiRetentionCleanup {
    private static final String ENDED = "('COMPLETED','FAILED','CANCELLED','INTERRUPTED')";
    private final EntityManager em;
    private final TransactionTemplate tx;
    private volatile boolean ready;
    public AiRetentionCleanup(EntityManager em, PlatformTransactionManager manager) {
        this.em = em; tx = new TransactionTemplate(manager); tx.setTimeout(5);
        tx.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }
    @EventListener(ApplicationReadyEvent.class)
    public void ready() { ready = true; }
    @Scheduled(fixedDelay = 60000, initialDelay = 60000)
    public void scheduled() {
        if (!ready) return;
        try { clean(); }
        catch (RuntimeException unavailable) { org.slf4j.LoggerFactory.getLogger(getClass()).warn("AI retention cleanup pending; next sweep will retry"); }
    }
    /** Formal job entry, also usable by isolated acceptance; deliberately has no user-facing route. */
    public void clean() {
        long now = System.currentTimeMillis();
        recoverAudits(now);
        var deliveries = em.createQuery("select a.id from CoreAiTaskAttempt a where a.status in " + ENDED + " and a.finishedAt is not null and a.deliveryExpiresAt<=:now and a.deliveryPurgedAt is null order by a.id", Long.class)
                .setParameter("now", now).setMaxResults(100).getResultList();
        for (var id : deliveries) tx.executeWithoutResult(status -> {
            // Same lock order as acceptance/retry/terminal paths. No active state or resource claim is mutated.
            em.find(CoreAiTaskSchedule.class, 1L, LockModeType.PESSIMISTIC_WRITE);
            var initial = em.find(CoreAiTaskAttempt.class, id);
            if (initial == null) return;
            var task = em.find(CoreAiTask.class, initial.getTaskId(), LockModeType.PESSIMISTIC_WRITE);
            int changed = em.createQuery("update CoreAiTaskAttempt a set a.resultJson=null, a.deliveryPurgedAt=:now, a.noticeAckAt=coalesce(a.noticeAckAt,:now) where a.id=:id and a.status in " + ENDED + " and a.finishedAt is not null and a.deliveryExpiresAt<=:now and a.deliveryPurgedAt is null")
                    .setParameter("id", id).setParameter("now", now).executeUpdate();
            if (changed != 1 || task == null) return;
            Long needed = em.createQuery("select count(a) from CoreAiTaskAttempt a where a.taskId=:task and (a.status not in " + ENDED + " or a.finishedAt is null or a.deliveryExpiresAt is null or a.deliveryExpiresAt>:now)", Long.class)
                    .setParameter("task", task.getId()).setParameter("now", now).getSingleResult();
            if (needed == 0) em.createQuery("update CoreAiTask t set t.inputJson='{}',t.contextJson='{}',t.ownerJson='{}' where t.id=:id")
                    .setParameter("id", task.getId()).executeUpdate();
            em.clear();
        });
        var audits = em.createQuery("select e.id from CoreAiAuditExecution e where e.finishedAt is not null and e.status in " + ENDED + " and e.retentionExpiresAt<=:now order by e.id", String.class)
                .setParameter("now", now).setMaxResults(100).getResultList();
        for (var id : audits) tx.executeWithoutResult(status -> {
            var row = em.find(CoreAiAuditExecution.class, id, LockModeType.PESSIMISTIC_WRITE);
            if (row == null || row.getFinishedAt() == null || row.getRetentionExpiresAt() == null || row.getRetentionExpiresAt() > now || !ended(row.getStatus())) return;
            // A linked running/queued attempt is protected even if malformed audit timestamps claim expiry.
            if (row.getAttemptId() != null && !em.createQuery("select a.id from CoreAiTaskAttempt a where str(a.id)=:id and (a.status not in " + ENDED + " or a.finishedAt is null)", Long.class)
                    .setParameter("id", row.getAttemptId()).setMaxResults(1).getResultList().isEmpty()) return;
            if (em.find(CoreAiAuditRetired.class, id) == null) { var retired = new CoreAiAuditRetired(); retired.setId(id); em.persist(retired); }
            // Children before parent in one transaction; a tombstone stops late entries and gap repair.
            em.createQuery("delete from CoreAiAuditEntry e where e.executionId=:id").setParameter("id", id).executeUpdate();
            em.remove(row); em.flush(); em.clear();
        });
    }
    private void recoverAudits(long now) {
        // A terminal background attempt is authoritative. Other executions are bounded by their frozen deadline.
        // Leave a short window for ordinary terminal callbacks to finish before compensating a missing callback.
        var ids = em.createQuery("select e.id from CoreAiAuditExecution e where e.finishedAt is null and "
                        + "(e.deadline<=:now or exists (select a.id from CoreAiTaskAttempt a where a.executionId=e.id "
                        + "and a.status in " + ENDED + " and a.finishedAt is not null and (a.status='INTERRUPTED' or a.finishedAt<=:settled))) order by e.id", String.class)
                .setParameter("now", now).setParameter("settled", now - 5000).setMaxResults(100).getResultList();
        for (String id : ids) tx.executeWithoutResult(status -> {
            var row = em.find(CoreAiAuditExecution.class, id, LockModeType.PESSIMISTIC_WRITE);
            if (row == null || row.getFinishedAt() != null) return;
            var attempts = em.createQuery("select a from CoreAiTaskAttempt a where a.executionId=:execution", CoreAiTaskAttempt.class)
                    .setParameter("execution", id).setMaxResults(1).getResultList();
            var attempt = attempts.isEmpty() ? null : attempts.get(0);
            if (attempt != null && (!ended(attempt.getStatus()) || attempt.getFinishedAt() == null)) return;
            if (attempt == null && (row.getDeadline() == null || row.getDeadline() > now)) return;
            if (row.getRetentionDays() == null || row.getRetentionDays() < 1) return;
            long finished = attempt == null ? row.getDeadline() : attempt.getFinishedAt();
            row.setStatus(attempt == null ? "INTERRUPTED" : attempt.getStatus()); row.setFinishedAt(finished);
            row.setRetentionExpiresAt(finished + java.util.concurrent.TimeUnit.DAYS.toMillis(row.getRetentionDays()));
            row.setIncomplete(true); row.setRecoveredAt(now);
            // Keep captured request/response and usage; do not invent output or alter already completed steps.
            em.createQuery("update CoreAiAuditEntry e set e.status='INTERRUPTED',e.finishedAt=:finished,e.incomplete=true,e.errorCode='interrupted' where e.executionId=:execution and e.finishedAt is null")
                    .setParameter("finished", finished).setParameter("execution", id).executeUpdate();
            em.flush();
        });
    }
    private boolean ended(String state) { return java.util.Set.of("COMPLETED", "FAILED", "CANCELLED", "INTERRUPTED").contains(state); }
}
