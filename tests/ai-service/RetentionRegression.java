package io.dataease.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.ai.manage.*;
import jakarta.persistence.EntityManager;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/** Approved public settings / production cleanup seams; isolated database observations are evidence only. */
public final class RetentionRegression {
    public static void run(AiConfigurationManage settings, AiAccess access, java.util.concurrent.atomic.AtomicBoolean permitted,
                           EntityManager em, org.springframework.transaction.PlatformTransactionManager manager,
                           org.springframework.jdbc.core.JdbcTemplate jdbc) throws Exception {
        var json = new ObjectMapper();
        var mvc = MockMvcBuilders.standaloneSetup(new io.dataease.ai.server.AiConfigurationServer(settings, null)).build();
        var current = json.readTree(mvc.perform(get("/ai/settings")).andReturn().getResponse().getContentAsString());
        if (current.path("retention").path("auditDays").asInt() != 30 || current.path("retention").path("deliveryDays").asInt() != 7)
            throw new AssertionError("public settings expose independent audit30/delivery7 retention defaults");
        var change = ((com.fasterxml.jackson.databind.node.ObjectNode) current).deepCopy();
        change.put("expectedVersion", current.path("rowVersion").asLong());
        change.putObject("retention").put("auditDays", 45).put("deliveryDays", 3);
        var saved = json.readTree(mvc.perform(post("/ai/settings").contentType("application/json").content(json.writeValueAsBytes(change))).andReturn().getResponse().getContentAsString());
        if (saved.path("retention").path("auditDays").asInt() != 45 || saved.path("retention").path("deliveryDays").asInt() != 3)
            throw new AssertionError("independent retention update persists");
        denied(() -> mvc.perform(post("/ai/settings").contentType("application/json").content(json.writeValueAsBytes(change))), "stale CAS must not overwrite retention");
        var legacy = ((com.fasterxml.jackson.databind.node.ObjectNode) saved).deepCopy(); legacy.remove("retention");
        legacy.put("expectedVersion", saved.path("rowVersion").asLong());
        var compatible = json.readTree(mvc.perform(post("/ai/settings").contentType("application/json").content(json.writeValueAsBytes(legacy))).andReturn().getResponse().getContentAsString());
        if (!compatible.path("retention").equals(saved.path("retention"))) throw new AssertionError("legacy settings requests preserve current retention");
        for (int invalid : new int[]{0, -1, 3651}) {
            var body = ((com.fasterxml.jackson.databind.node.ObjectNode) compatible).deepCopy(); body.put("expectedVersion", compatible.path("rowVersion").asLong());
            body.putObject("retention").put("auditDays", invalid).put("deliveryDays", 7);
            denied(() -> mvc.perform(post("/ai/settings").contentType("application/json").content(json.writeValueAsBytes(body))), "invalid retention rejects");
        }
        permitted.set(false);
        try { denied(() -> mvc.perform(get("/ai/settings")), "ordinary user cannot read administrator retention settings"); }
        finally { permitted.set(true); }
        var cleanupClass = Class.forName("io.dataease.ai.manage.AiRetentionCleanup");
        var cleanup = cleanupClass.getConstructor(EntityManager.class, org.springframework.transaction.PlatformTransactionManager.class).newInstance(em, manager);
        long now = System.currentTimeMillis();
        jdbc.update("insert into core_ai_task (id,current_attempt_id,owner_binding,submission_hash,input_hash,owner_json,input_json,context_json,created_at,actor_id) values(9001,9001,'owner','retention-task','hash','{}','input-body','context-body',?,1)", now);
        jdbc.update("insert into core_ai_task_attempt (id,task_id,attempt_number,retry_hash,status,created_at,finished_at,delivery_expires_at,result_json,retry_safe) values(9001,9001,1,'retention-attempt','COMPLETED',?,?,?,'secret-result',true)", now-1000, now-500, now-1);
        cleanupClass.getMethod("clean").invoke(cleanup);
        if (jdbc.queryForObject("select result_json from core_ai_task_attempt where id=9001", String.class) != null)
            throw new AssertionError("production cleanup explicitly removes expired ended result");
        if (!"{}".equals(jdbc.queryForObject("select input_json from core_ai_task where id=9001", String.class)))
            throw new AssertionError("expired task keeps idempotency tombstone but no input body");
        cleanupClass.getMethod("clean").invoke(cleanup);
        delivery(settings, access, em, manager, jdbc, cleanupClass, cleanup);
        audits(access, em, manager, jdbc, cleanupClass, cleanup);
        partialDelivery(em, manager, jdbc, cleanupClass, cleanup);
        System.out.println("PASS retention production cleanup is repeatable and clears expired delivery bodies");
        System.out.println("PASS retention defaults, independent persistence, validation, legacy compatibility and CAS through public settings HTTP");
        RetentionSchemaRegression.run();
    }
    private static void delivery(AiConfigurationManage settings, AiAccess access, EntityManager em,
                                 org.springframework.transaction.PlatformTransactionManager manager, org.springframework.jdbc.core.JdbcTemplate jdbc,
                                 Class<?> cleanupClass, Object cleanup) throws Exception {
        var beans = new org.springframework.beans.factory.support.StaticListableBeanFactory(); var json = new ObjectMapper();
        var admissions = new java.util.concurrent.atomic.AtomicInteger();
        beans.addBean("runtime", new AiRuntimeManage(beans.getBeanProvider(io.dataease.api.ai.runtime.AiFeature.class), settings, null, null, null, json, access) {
            @Override public void verifyTaskAdmission(AiAccess.Identity owner) { admissions.incrementAndGet(); }
        });
        beans.addBean("scene", new AiConversationScene() {
            public String code() { return "retention"; }
            public java.util.List<Resource> resources() { return java.util.List.of(new Resource("11", "owned")); }
            public Context resolve(String resource, String input) { return new Context(resource, "owned", "v1", "private-context"); }
            public void authorize(Context context) { if (!"11".equals(context.resourceId())) throw io.dataease.ai.support.AiErrors.error("forbidden"); }
            public java.util.List<ResourceClaim> resourceClaims(Context context) { return java.util.List.of(new ResourceClaim("11", context.instruction(), "retention")); }
        });
        var tasks = new AiTaskManage(em, manager, beans.getBeanProvider(AiConversationScene.class), beans.getBeanProvider(AiRuntimeManage.class), access, json);
        var mvc = MockMvcBuilders.standaloneSetup(new io.dataease.ai.server.AiTaskServer(tasks)).setControllerAdvice(new io.dataease.exception.GlobalExceptionHandler()).build();
        try {
            String body = "{\"submissionKey\":\"retention-expired\",\"scene\":\"retention\",\"capability\":\"retention.assist\",\"resourceId\":\"11\",\"editorId\":\"editor\",\"text\":\"private-input\"}";
            var expired = json.readTree(mvc.perform(post("/ai/tasks").contentType("application/json").content(body)).andReturn().getResponse().getContentAsString());
            String path = "/ai/tasks/" + expired.path("taskId").asText(); long attempt = expired.path("attemptId").asLong();
            mvc.perform(post(path + "/cancel"));
            long finished = jdbc.queryForObject("select finished_at from core_ai_task_attempt where id=?", Long.class, attempt);
            if (jdbc.queryForObject("select delivery_expires_at from core_ai_task_attempt where id=?", Long.class, attempt) != finished + java.util.concurrent.TimeUnit.DAYS.toMillis(3))
                throw new AssertionError("new terminal attempt uses configured delivery retention");
            // Acknowledgement is idempotent and never deletes result or administrator audit evidence.
            mvc.perform(post("/ai/tasks/notices/" + attempt + "/ack")); mvc.perform(post("/ai/tasks/notices/" + attempt + "/ack"));
            jdbc.update("update core_ai_task_attempt set delivery_expires_at=?, result_json='private-result' where id=?", System.currentTimeMillis()-1, attempt);
            var modernExpired = mvc.perform(get(path).header("X-DE-Error-Contract", "2")).andReturn().getResponse();
            var legacyExpired = mvc.perform(get(path)).andReturn().getResponse();
            if (modernExpired.getStatus() != 404 || legacyExpired.getStatus() != 409 || !modernExpired.getContentAsString().contains("i18n_ai_result_expired"))
                throw new AssertionError("expired owner delivery has explicit NOT_FOUND404 modern / compatible409 legacy semantics: " + modernExpired.getStatus() + "/" + legacyExpired.getStatus());
            var pending = json.readTree(mvc.perform(get("/ai/tasks/notices")).andReturn().getResponse().getContentAsString());
            if (pending.toString().contains(Long.toString(attempt))) throw new AssertionError("expired notice unavailable before sweep");
            var configMvc = MockMvcBuilders.standaloneSetup(new io.dataease.ai.server.AiConfigurationServer(settings,null)).build();
            var extended = (com.fasterxml.jackson.databind.node.ObjectNode) json.readTree(configMvc.perform(get("/ai/settings")).andReturn().getResponse().getContentAsString());
            extended.put("expectedVersion", extended.path("rowVersion").asLong());
            extended.putObject("retention").put("auditDays",45).put("deliveryDays",3650);
            configMvc.perform(post("/ai/settings").contentType("application/json").content(json.writeValueAsBytes(extended)));
            if (mvc.perform(get(path).header("X-DE-Error-Contract","2")).andReturn().getResponse().getStatus() != 404)
                throw new AssertionError("longer retention must never revive an already expired result");
            cleanupClass.getMethod("clean").invoke(cleanup);
            int accepted = admissions.get();
            denied(() -> mvc.perform(post("/ai/tasks").contentType("application/json").content(body)), "same submit after cleanup must not execute again");
            denied(() -> mvc.perform(get(path)), "cleaned result stays unavailable");
            denied(() -> mvc.perform(post(path + "/retry").contentType("application/json").content("{\"retryKey\":\"manual\",\"expectedAttemptId\":\"" + attempt + "\"}")), "expired manual retry denied");
            denied(() -> mvc.perform(post("/ai/tasks/notices/" + attempt + "/ack")), "expired ACK denied safely");
            denied(() -> mvc.perform(post(path + "/conversation").contentType("application/json").content("{\"sessionId\":\"old\",\"turnId\":\"old\"}")), "expired conversation cannot recover body");
            var cancelled = mvc.perform(post(path + "/cancel")).andReturn().getResponse();
            if (cancelled.getStatus() != 200 || cancelled.getContentAsString().contains("private-result") || json.readTree(cancelled.getContentAsString()).path("retryEligible").asBoolean() || admissions.get() != accepted)
                throw new AssertionError("expired cancel returns metadata only, old submit/retry never admits new work");
            // Preserve a queued task and its exact resource claim, even with corrupt past expiry timestamps.
            String queuedBody = body.replace("retention-expired", "retention-queued");
            var queued = json.readTree(mvc.perform(post("/ai/tasks").contentType("application/json").content(queuedBody)).andReturn().getResponse().getContentAsString());
            long queuedId = queued.path("attemptId").asLong(), taskId = queued.path("taskId").asLong();
            jdbc.update("update core_ai_task_attempt set delivery_expires_at=1 where id=?", queuedId);
            jdbc.update("insert into core_ai_task_attempt(id,task_id,attempt_number,retry_hash,status,created_at,finished_at,delivery_expires_at,result_json,retry_safe) values(9002,?,0,'mixed-expired','FAILED',1,2,3,'expired-older-attempt',true)", taskId);
            String preserved = jdbc.queryForObject("select input_json from core_ai_task where id=?", String.class, taskId);
            cleanupClass.getMethod("clean").invoke(cleanup);
            if (!preserved.equals(jdbc.queryForObject("select input_json from core_ai_task where id=?", String.class, taskId))
                    || jdbc.queryForObject("select count(*) from core_ai_task_resource where attempt_id=?", Long.class, queuedId) != 1
                    || jdbc.queryForObject("select delivery_purged_at from core_ai_task_attempt where id=?", Long.class, queuedId) != null
                    || jdbc.queryForObject("select result_json from core_ai_task_attempt where id=9002", String.class) != null)
                throw new AssertionError("mixed attempts clear old body but preserve queued input, identity and claim");
            jdbc.update("update core_ai_task_attempt set status='RUNNING',node_id='owned-node',fence='owned-fence',finished_at=2,delivery_expires_at=3 where id=?", queuedId);
            cleanupClass.getMethod("clean").invoke(cleanup);
            if (!"RUNNING".equals(jdbc.queryForObject("select status from core_ai_task_attempt where id=?", String.class, queuedId))
                    || !"owned-fence".equals(jdbc.queryForObject("select fence from core_ai_task_attempt where id=?", String.class, queuedId))
                    || jdbc.queryForObject("select count(*) from core_ai_task_resource where attempt_id=?", Long.class, queuedId) != 1)
                throw new AssertionError("running identity/fence/claim survive malformed expiry");
            // Ended but unexpired body remains usable, including after acknowledging the notice.
            jdbc.update("update core_ai_task_attempt set status='COMPLETED',finished_at=?,delivery_expires_at=?,result_json=?,fence=null,node_id=null where id=?", System.currentTimeMillis(), System.currentTimeMillis()+86400000,
                    "{\"type\":\"EXPLANATION\",\"text\":\"retained-result\"}", queuedId);
            jdbc.update("delete from core_ai_task_resource where attempt_id=?", queuedId);
            mvc.perform(post("/ai/tasks/notices/" + queuedId + "/ack")); cleanupClass.getMethod("clean").invoke(cleanup);
            if (!mvc.perform(get("/ai/tasks/" + taskId)).andReturn().getResponse().getContentAsString().contains("retained-result")) throw new AssertionError("unexpired result survives notice ACK and cleanup");
            jdbc.update("insert into core_ai_audit_execution(id,attempt_id,status,finished_at,retention_expires_at) values('active-linked-audit',?,'COMPLETED',1,2)", Long.toString(queuedId));
            jdbc.update("update core_ai_task_attempt set status='RUNNING',node_id='owned-node',fence='owned-fence' where id=?",queuedId);
            cleanupClass.getMethod("clean").invoke(cleanup);
            if (jdbc.queryForObject("select count(*) from core_ai_audit_execution where id='active-linked-audit'", Long.class) != 1)
                throw new AssertionError("malformed ended audit never deletes evidence linked to active attempt");
            jdbc.update("update core_ai_task_attempt set status='COMPLETED',node_id=null,fence=null where id=?",queuedId);
            cleanupClass.getMethod("clean").invoke(cleanup);
            System.out.println("PASS retention public terminal expiry, pre-sweep rejection, post-sweep submit/get/retry/origin/ACK/cancel, mixed attempts and queued/running resource protection");
        } finally { tasks.destroy(); }
    }
    private static void audits(AiAccess access, EntityManager em, org.springframework.transaction.PlatformTransactionManager manager,
                               org.springframework.jdbc.core.JdbcTemplate jdbc, Class<?> cleanupClass, Object cleanup) throws Exception {
        long now = System.currentTimeMillis();
        for (String id : new String[]{"retained-audit", "expired-audit"}) {
            jdbc.update("insert into core_ai_audit_execution(id,status,finished_at,retention_expires_at,incomplete,gap_count,last_sequence) values(?,'COMPLETED',?,?,false,0,1)", id, now-1000, id.startsWith("expired") ? now-1 : now+86400000);
            jdbc.update("insert into core_ai_audit_entry(id,execution_id,sequence,request_body,response_body) values(?,?,1,'private-request','private-response')", id.startsWith("expired") ? 9101 : 9102, id);
        }
        var fail = new java.util.concurrent.atomic.AtomicBoolean(true);
        var failing = new org.springframework.orm.jpa.JpaTransactionManager(em.getEntityManagerFactory()) {
            @Override protected void doCommit(org.springframework.transaction.support.DefaultTransactionStatus status) {
                if (fail.getAndSet(false)) throw new org.springframework.transaction.TransactionSystemException("owned cleanup commit failure");
                super.doCommit(status);
            }
        }; failing.setRollbackOnCommitFailure(true);
        var faultCleanup = cleanupClass.getConstructor(EntityManager.class, org.springframework.transaction.PlatformTransactionManager.class).newInstance(em, failing);
        try { cleanupClass.getMethod("clean").invoke(faultCleanup); throw new AssertionError("cleanup failure was hidden"); }
        catch (java.lang.reflect.InvocationTargetException expected) { }
        if (jdbc.queryForObject("select count(*) from core_ai_audit_execution where id='expired-audit'", Long.class) != 1
                || jdbc.queryForObject("select count(*) from core_ai_audit_entry where execution_id='expired-audit'", Long.class) != 1
                || jdbc.queryForObject("select count(*) from core_ai_audit_retired where id='expired-audit'", Long.class) != 0)
            throw new AssertionError("audit child/parent/tombstone deletion is atomic on mid-sweep failure");
        cleanupClass.getMethod("clean").invoke(cleanup); cleanupClass.getMethod("clean").invoke(cleanup);
        if (jdbc.queryForObject("select count(*) from core_ai_audit_execution where id='expired-audit'", Long.class) != 0
                || jdbc.queryForObject("select count(*) from core_ai_audit_entry where execution_id='expired-audit'", Long.class) != 0
                || jdbc.queryForObject("select count(*) from core_ai_audit_execution where id='retained-audit'", Long.class) != 1
                || jdbc.queryForObject("select count(*) from core_ai_audit_entry where execution_id='retained-audit'", Long.class) != 1)
            throw new AssertionError("retry cleans expired audit and preserves independent unexpired audit");
        var auditAccess = new AiAccess(null, null) {
            public Identity capture() { return access.capture(); }
            public void verify(Identity identity, boolean configuration) { access.verify(identity, configuration); }
            public AiDeploymentMode.State mode() { return access.mode(); }
            public void audit(Identity identity, Long organization) { access.verify(identity, false); }
        };
        var outage = new java.util.concurrent.atomic.AtomicBoolean();
        var auditTransactions = new org.springframework.orm.jpa.JpaTransactionManager(em.getEntityManagerFactory()) {
            @Override protected void doCommit(org.springframework.transaction.support.DefaultTransactionStatus status) {
                if (outage.get()) throw new org.springframework.transaction.TransactionSystemException("owned pure audit outage");
                super.doCommit(status);
            }
        }; auditTransactions.setRollbackOnCommitFailure(true);
        var audit = new AiAuditManage(em, auditTransactions, auditAccess, new ObjectMapper());
        try {
            var mvc = MockMvcBuilders.standaloneSetup(new io.dataease.ai.server.AiAuditServer(audit)).build();
            if (!mvc.perform(get("/ai/audit/retained-audit")).andReturn().getResponse().getContentAsString().contains("private-response")) throw new AssertionError("unexpired administrator audit body remains readable");
            var session = audit.session(new AuditContext("late-audit"), new AiRuntimeManage.ModelSnapshot(1L,1L,1L,"owned-model"), 1L, null, "owned-resource", 45);
            var exchange = session.begin("MODEL", "late-request", null); exchange.output("late-response"); exchange.finish(true, null); session.end("COMPLETED");
            if (jdbc.queryForObject("select retention_expires_at-finished_at from core_ai_audit_execution where id='late-audit'", Long.class) != java.util.concurrent.TimeUnit.DAYS.toMillis(45)) throw new AssertionError("audit keeps its own configured retention snapshot");
            outage.set(true); session.begin("MODEL", "failed-late-body", null).finish(true, null);
            if (audit.pendingGapCount() != 1) throw new AssertionError("controlled audit outage creates bounded metadata repair marker");
            jdbc.update("update core_ai_audit_execution set retention_expires_at=? where id='late-audit'", now-1);
            outage.set(false);
            denied(() -> mvc.perform(get("/ai/audit/late-audit")), "audit expires closed before physical cleanup");
            cleanupClass.getMethod("clean").invoke(cleanup);
            long until = System.currentTimeMillis()+6500;
            while (audit.pendingGapCount() != 0 && System.currentTimeMillis()<until) Thread.sleep(50);
            if (audit.pendingGapCount() != 0 || jdbc.queryForObject("select count(*) from core_ai_audit_execution where id='late-audit'", Long.class) != 0
                    || jdbc.queryForObject("select count(*) from core_ai_audit_entry where execution_id='late-audit'", Long.class) != 0)
                throw new AssertionError("late entry and scheduled metadata repair cannot recreate expired audit");
            session.begin("MODEL", "late-body-must-not-return", null).finish(true, null);
            if (jdbc.queryForObject("select count(*) from core_ai_audit_entry where execution_id='late-audit'", Long.class) != 0) throw new AssertionError("late entry cannot bypass retired execution");
            denied(() -> mvc.perform(get("/ai/audit/late-audit")), "retired audit detail never returns body");
        } finally { audit.close(); }
        System.out.println("PASS retention independent audit expiry, atomic child-parent removal, fault retry, scheduled gap repair and late-write retirement");
    }
    private static void partialDelivery(EntityManager em, org.springframework.transaction.PlatformTransactionManager manager,
                                        org.springframework.jdbc.core.JdbcTemplate jdbc, Class<?> type, Object cleanup) throws Exception {
        for (long id : new long[]{9201,9202}) {
            jdbc.update("insert into core_ai_task(id,current_attempt_id,owner_binding,submission_hash,input_hash,owner_json,input_json,context_json,created_at) values(?,?,'owner',?,'hash','{}','partial-input','partial-context',1)",id,id,"partial-"+id);
            jdbc.update("insert into core_ai_task_attempt(id,task_id,attempt_number,retry_hash,status,created_at,finished_at,delivery_expires_at,result_json,retry_safe) values(?,?,1,?,'COMPLETED',1,2,3,'partial-result',true)",id,id,"partial-attempt-"+id);
        }
        var commits = new java.util.concurrent.atomic.AtomicInteger();
        var fault = new org.springframework.orm.jpa.JpaTransactionManager(em.getEntityManagerFactory()) {
            @Override protected void doCommit(org.springframework.transaction.support.DefaultTransactionStatus status) {
                if (commits.incrementAndGet() == 2) throw new org.springframework.transaction.TransactionSystemException("owned second delivery commit failure");
                super.doCommit(status);
            }
        }; fault.setRollbackOnCommitFailure(true);
        try { type.getMethod("clean").invoke(type.getConstructor(EntityManager.class,org.springframework.transaction.PlatformTransactionManager.class).newInstance(em,fault)); throw new AssertionError("fault did not execute"); }
        catch (java.lang.reflect.InvocationTargetException expected) { }
        if (jdbc.queryForObject("select result_json from core_ai_task_attempt where id=9201",String.class) != null
                || !"partial-result".equals(jdbc.queryForObject("select result_json from core_ai_task_attempt where id=9202",String.class)))
            throw new AssertionError("completed cleanup item commits; failed next item rolls back");
        var errors = new java.util.concurrent.ConcurrentLinkedQueue<Throwable>();
        Runnable run = () -> { try { type.getMethod("clean").invoke(cleanup); } catch (Throwable conflict) { errors.add(conflict); } };
        var first = new Thread(run,"owned-retention-one"); var second = new Thread(run,"owned-retention-two"); first.start(); second.start(); first.join(5000); second.join(5000);
        if (first.isAlive() || second.isAlive()) throw new AssertionError("concurrent cleanup did not finish");
        type.getMethod("clean").invoke(cleanup);
        if (jdbc.queryForObject("select result_json from core_ai_task_attempt where id=9202",String.class) != null
                || !"{}".equals(jdbc.queryForObject("select input_json from core_ai_task where id=9202",String.class))) throw new AssertionError("concurrent retry leaves final delivery tombstone consistent");
        if (!errors.isEmpty()) throw new AssertionError("concurrent cleanup must remain idempotent",errors.peek());
        System.out.println("PASS retention partial delivery commit failure, bounded retry and concurrent cleanup consistency");
    }
    private record AuditContext(String executionId) implements io.dataease.api.ai.runtime.AiExecutionContext {
        public String feature() { return "owned-retention"; }
        public Long actorId() { return 1L; }
        public Long organizationId() { return null; }
        public boolean background() { return false; }
        public Long proxyOrganizationId() { return null; }
        public long deadline() { return System.currentTimeMillis()+10000; }
        public io.dataease.api.ai.runtime.AiDataScope scope() { return null; }
    }
    @FunctionalInterface private interface Request { org.springframework.test.web.servlet.ResultActions run() throws Exception; }
    private static void denied(Request request, String reason) throws Exception {
        try { if (request.run().andReturn().getResponse().getStatus() >= 400) return; }
        catch (jakarta.servlet.ServletException expected) {
            if (expected.getRootCause() instanceof io.dataease.ai.support.AiErrors.Failure) return;
            throw expected;
        }
        throw new AssertionError(reason);
    }
}
