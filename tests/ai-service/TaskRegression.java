package io.dataease.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.ai.manage.*;
import io.dataease.ai.server.AiTaskServer;
import io.dataease.ai.server.AiConversationServer;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import java.util.*;

/** Public task HTTP contract. Business identity and resource are controlled fixtures. */
public final class TaskRegression {
    private static com.fasterxml.jackson.databind.JsonNode await(org.springframework.test.web.servlet.MockMvc mvc, ObjectMapper json, String path, String status) throws Exception {
        com.fasterxml.jackson.databind.JsonNode result = null;
        for (int i = 0; i < 240; i++) {
            result = json.readTree(mvc.perform(get(path)).andReturn().getResponse().getContentAsString());
            if (status.equals(result.path("status").asText())) return result;
            Thread.sleep(25);
        }
        throw new AssertionError("task expected " + status + ": " + result);
    }
    public static void run(EntityManager em, PlatformTransactionManager manager, AiAccess access, AiConfigurationManage configuration, io.dataease.ai.dao.repository.CoreAiOrgPolicyRepository policies, io.dataease.ai.support.AiSecrets keys, io.dataease.ai.support.AiAdmission admission) throws Exception {
        var sceneAllowed = new java.util.concurrent.atomic.AtomicBoolean(true);
        var toolsOn = new java.util.concurrent.atomic.AtomicBoolean();
        var executed = new java.util.concurrent.atomic.AtomicInteger();
        var beans = new StaticListableBeanFactory();
        beans.addBean("scene", new AiConversationScene() {
            public String code() { return "sql"; }
            public List<Resource> resources() { return List.of(); }
            public Context resolve(String id, String input) { return new Context(id, "fixture", "v1", input); }
            public void authorize(Context context) {
                if (!sceneAllowed.get() || !Objects.equals(io.dataease.permission.util.V3UserUtil.getUid(), 1L)) throw io.dataease.ai.support.AiErrors.error("forbidden");
            }
            public io.dataease.api.ai.conversation.AiConversationDtos.Result result(Context context, String text, List<io.dataease.api.ai.conversation.AiConversationDtos.Tool> tools, String editor, String draft) {
                return new io.dataease.api.ai.conversation.AiConversationDtos.Result("SQL_SUGGESTION", text, "select 1", context.resourceId(), context.resourceName(), context.revision(), editor, draft);
            }
            public List<io.dataease.ai.support.AiToolProtocol.Definition> tools(Context context) {
                return toolsOn.get() ? List.of(new io.dataease.ai.support.AiToolProtocol.Definition("inspect", "inspect authorized metadata", Map.of())) : List.of();
            }
            public String execute(Context context, String name, Map<String, String> arguments) {
                if (org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()) throw new AssertionError("tool I/O must not hold metadata transaction");
                authorize(context); executed.incrementAndGet(); return "{\"tables\":[\"authorized_table\"]}";
            }
        });
        var followed = new java.util.concurrent.atomic.AtomicBoolean();
        var stateSends = new java.util.concurrent.atomic.AtomicInteger();
        var failCommit = new java.util.concurrent.atomic.AtomicBoolean();
        var failingManager = new org.springframework.orm.jpa.JpaTransactionManager(em.getEntityManagerFactory()) {
            @Override protected void doCommit(org.springframework.transaction.support.DefaultTransactionStatus status) {
                if (failCommit.get() && !Thread.currentThread().getName().equals("de-ai-task-heartbeat")) throw new org.springframework.transaction.TransactionSystemException("controlled unavailable commit");
                super.doCommit(status);
            }
        };
        failingManager.setRollbackOnCommitFailure(true);
        var conversations = new AiConversationManage(beans.getBeanProvider(AiConversationScene.class), beans.getBeanProvider(AiRuntimeManage.class), access);
        beans.addBean("conversations", conversations);
        var chatMvc = MockMvcBuilders.standaloneSetup(new AiConversationServer(conversations)).build();
        var tasks = new AiTaskManage(em, failingManager, beans.getBeanProvider(AiConversationScene.class),
                beans.getBeanProvider(AiRuntimeManage.class), access, new ObjectMapper(), beans.getBeanProvider(AiConversationManage.class));
        var json = new ObjectMapper();
        var transport = new io.dataease.ai.support.AiTransport(new io.dataease.ai.support.AiOutboundPolicy(), admission, json);
        beans.addBean("tasks", tasks);
        beans.addBean("runtime", new AiRuntimeManage(beans.getBeanProvider(io.dataease.api.ai.runtime.AiFeature.class), configuration, policies, keys, transport, json, access));
        var mvc = MockMvcBuilders.standaloneSetup(new AiTaskServer(tasks)).build();
        String request = "{\"submissionKey\":\"same-submit\",\"scene\":\"sql\",\"capability\":\"sql.assist\",\"resourceId\":\"11\",\"editorId\":\"editor\",\"sql\":\"select 1\",\"text\":\"explain\"}";
        var first = mvc.perform(post("/ai/tasks").contentType("application/json").content(request)).andReturn().getResponse();
        if (first.getStatus() != 200) throw new AssertionError("task accepted");
        var a = json.readTree(first.getContentAsString());
        if (!a.path("taskId").isTextual() || !a.path("attemptId").isTextual()) throw new AssertionError("Snowflake IDs preserve exact browser precision");
        var b = json.readTree(mvc.perform(post("/ai/tasks").contentType("application/json").content(request)).andReturn().getResponse().getContentAsString());
        if (!a.path("taskId").equals(b.path("taskId")) || !"QUEUED".equals(a.path("status").asText())) throw new AssertionError("same submission returns the durable queued task");
        var read = json.readTree(mvc.perform(get("/ai/tasks/" + a.path("taskId").asText())).andReturn().getResponse().getContentAsString());
        if (!read.path("attemptId").equals(a.path("attemptId"))) throw new AssertionError("public task read identifies current attempt");
        String path = "/ai/tasks/" + a.path("taskId").asText();
        var cancelled = json.readTree(mvc.perform(post(path + "/cancel")).andReturn().getResponse().getContentAsString());
        if (!"CANCELLED".equals(cancelled.path("status").asText())) throw new AssertionError("queued cancellation is durable");
        var notices = json.readTree(mvc.perform(get("/ai/tasks/notices")).andReturn().getResponse().getContentAsString());
        if (notices.size() != 1 || !"CANCELLED".equals(notices.get(0).path("status").asText())) throw new AssertionError("offline cancelled task has durable completion notice");
        mvc.perform(post("/ai/tasks/notices/" + a.path("attemptId").asText() + "/ack"));
        mvc.perform(post("/ai/tasks/notices/" + a.path("attemptId").asText() + "/ack"));
        if (json.readTree(mvc.perform(get("/ai/tasks/notices")).andReturn().getResponse().getContentAsString()).size() != 0) throw new AssertionError("acknowledged notice is not redelivered");
        String retry = json.writeValueAsString(Map.of("retryKey", "retry-once", "expectedAttemptId", a.path("attemptId").asText()));
        var retried = json.readTree(mvc.perform(post(path + "/retry").contentType("application/json").content(retry)).andReturn().getResponse().getContentAsString());
        var repeatedRetry = json.readTree(mvc.perform(post(path + "/retry").contentType("application/json").content(retry)).andReturn().getResponse().getContentAsString());
        if (retried.path("attemptNumber").asInt() != 2 || !retried.path("attemptId").equals(repeatedRetry.path("attemptId"))) throw new AssertionError("retry increments attempt once and is idempotent");
        boolean retryConflict = false;
        try {
            mvc.perform(post(path + "/retry").contentType("application/json").content(json.writeValueAsString(Map.of("retryKey", "retry-once", "expectedAttemptId", retried.path("attemptId").asText()))));
        } catch (jakarta.servlet.ServletException expected) { retryConflict = expected.getCause() instanceof io.dataease.ai.support.AiErrors.Failure; }
        if (!retryConflict) throw new AssertionError("retry key cannot replay a different source attempt");
        boolean conflict = false;
        try { mvc.perform(post("/ai/tasks").contentType("application/json").content(request.replace("explain", "different"))); }
        catch (jakarta.servlet.ServletException expected) { conflict = expected.getCause() instanceof io.dataease.ai.support.AiErrors.Failure; }
        if (!conflict) throw new AssertionError("same submission key cannot overwrite changed input");
        var changed = json.readTree(mvc.perform(post("/ai/tasks").contentType("application/json").content(request.replace("same-submit", "new-submit").replace("explain", "different"))).andReturn().getResponse().getContentAsString());
        if (changed.path("taskId").equals(a.path("taskId"))) throw new AssertionError("changed input with fresh submission creates a new task");
        mvc.perform(post(path + "/cancel"));
        mvc.perform(post("/ai/tasks/" + changed.path("taskId").asText() + "/cancel"));
        var bounded = new ArrayList<String>();
        for (int i = 0; i < 10; i++) {
            var accepted = json.readTree(mvc.perform(post("/ai/tasks").contentType("application/json").content(request.replace("same-submit", "bounded-" + i))).andReturn().getResponse().getContentAsString());
            bounded.add("/ai/tasks/" + accepted.path("taskId").asText());
        }
        if (json.readTree(mvc.perform(get("/ai/tasks")).andReturn().getResponse().getContentAsString()).size() != 10) throw new AssertionError("only current owner's bounded active tasks listed");
        boolean boundedReject = false;
        try { mvc.perform(post("/ai/tasks").contentType("application/json").content(request.replace("same-submit", "bounded-eleventh"))); }
        catch (jakarta.servlet.ServletException expected) { boundedReject = expected.getCause() instanceof io.dataease.ai.support.AiErrors.Failure; }
        if (!boundedReject) throw new AssertionError("eleventh queued task rejected before unbounded admission");
        for (String boundedPath : bounded) mvc.perform(post(boundedPath + "/cancel"));
        var server = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            var modelRequest = json.readTree(exchange.getRequestBody());
            if (modelRequest.path("messages").toString().contains("follow-background")) {
                String messages = modelRequest.path("messages").toString();
                long questions = java.util.stream.StreamSupport.stream(modelRequest.path("messages").spliterator(), false)
                        .filter(message -> "user".equals(message.path("role").asText()) && "background-question".equals(message.path("content").asText())).count();
                boolean candidate = java.util.stream.StreamSupport.stream(modelRequest.path("messages").spliterator(), false)
                        .anyMatch(message -> "assistant".equals(message.path("role").asText()) && message.path("content").asText().contains("SQL:\nselect 1"));
                followed.set(questions == 1 && messages.contains("durable answer") && candidate);
            }
            if (modelRequest.path("messages").toString().contains("state-save-failure")) { stateSends.incrementAndGet(); failCommit.set(true); }
            boolean needsTool = modelRequest.has("tools") && !modelRequest.path("messages").toString().contains("authorized_table");
            String toolFrame = "data: {\"choices\":[{\"index\":0,\"delta\":{\"tool_calls\":[{\"index\":0,\"id\":\"inspect-1\",\"type\":\"function\",\"function\":{\"name\":\"inspect\",\"arguments\":\"{}\"}}]},\"finish_reason\":\"tool_calls\"}]}\n\ndata: [DONE]\n\n";
            byte[] body = (needsTool ? toolFrame : ("data: {\"choices\":[{\"index\":0,\"delta\":{\"content\":\"durable answer\"},\"finish_reason\":\"stop\"}]}\n\ndata: [DONE]\n\n")).getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/event-stream"); exchange.sendResponseHeaders(200, body.length);
            try { exchange.getResponseBody().write(body); } finally { exchange.close(); }
        });
        server.start();
        Long previous = configuration.setting().getDefaultModelId();
        try {
            var draft = configuration.draft(); var candidate = new io.dataease.api.ai.config.AiConfigDtos.Candidate();
            candidate.setId(draft.id()); candidate.setDraftHandle(draft.draftHandle()); candidate.setName("Task synthetic");
            candidate.setProvider("OPENAI_COMPATIBLE"); candidate.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/v1");
            candidate.setModelIdentifier("task-model-v1"); candidate.setApiKey("synthetic-key");
            candidate.setTestProof(configuration.testProof(configuration.candidate(candidate, true))); var saved = configuration.save(candidate, true);
            var setting = configuration.setting();
            configuration.saveSettings(new io.dataease.api.ai.config.AiConfigDtos.SettingsChange(setting.getRowVersion(), true, saved.id(), false, 60, 30000, 4096, 1, 1, configuration.impact("SETTINGS", saved.id() + ":true").token()));
            beans.addBean("tasks", tasks);
            beans.addBean("runtime", new AiRuntimeManage(beans.getBeanProvider(io.dataease.api.ai.runtime.AiFeature.class), configuration, policies, keys, transport, json, access));
            var submitted = json.readTree(mvc.perform(post("/ai/tasks").contentType("application/json").content(request.replace("same-submit", "background"))).andReturn().getResponse().getContentAsString());
            String backgroundPath = "/ai/tasks/" + submitted.path("taskId").asText();
            // Saturate I pool: a true detached task must use B pool and cannot require a polling lease.
            try (var interactive = admission.acquire(configuration.setting(), false, System.currentTimeMillis() + 30000)) {
                tasks.start();
                Thread.sleep(1000);
                com.fasterxml.jackson.databind.JsonNode done = null;
                for (int i = 0; i < 200; i++) {
                    done = json.readTree(mvc.perform(get(backgroundPath)).andReturn().getResponse().getContentAsString());
                    if ("COMPLETED".equals(done.path("status").asText())) break;
                    Thread.sleep(25);
                }
                if (!"COMPLETED".equals(done.path("status").asText()) || !"durable answer".equals(done.path("result").path("text").asText())
                        || !"task-model-v1".equals(done.path("modelIdentifier").asText())) throw new AssertionError("detached durable completion with pinned model through B pool: " + done);
            }
            var origin = json.readTree(chatMvc.perform(post("/ai/conversations").contentType("application/json").content("{\"scene\":\"sql\",\"resourceId\":\"11\",\"editorId\":\"editor\",\"sql\":\"select 1\"}")).andReturn().getResponse().getContentAsString());
            String originRequest = request.replace("same-submit", "origin-task").replace("\"text\":\"explain\"", "\"text\":\"background-question\",\"origin\":{\"sessionId\":\"" + origin.path("sessionId").asText() + "\",\"contextVersion\":\"" + origin.path("contextVersion").asText() + "\"}");
            var linked = json.readTree(mvc.perform(post("/ai/tasks").contentType("application/json").content(originRequest)).andReturn().getResponse().getContentAsString());
            String linkedPath = "/ai/tasks/" + linked.path("taskId").asText();
            await(mvc, json, linkedPath, "COMPLETED");
            mvc.perform(post("/ai/tasks/notices/" + linked.path("attemptId").asText() + "/ack"));
            var received = mvc.perform(post(linkedPath + "/conversation").contentType("application/json").content("{\"sessionId\":\"" + origin.path("sessionId").asText() + "\",\"contextVersion\":\"" + origin.path("contextVersion").asText() + "\"}")).andReturn().getResponse();
            if (received.getStatus() != 200) throw new AssertionError("still open original conversation accepts background result independently of notice acknowledgement");
            mvc.perform(post(linkedPath + "/conversation").contentType("application/json").content("{\"sessionId\":\"" + origin.path("sessionId").asText() + "\",\"contextVersion\":\"" + origin.path("contextVersion").asText() + "\"}"));
            boolean originDenied = false;
            try { mvc.perform(post(linkedPath + "/conversation").contentType("application/json").content("{\"sessionId\":\"other-session\",\"contextVersion\":\"other-version\"}")); }
            catch (jakarta.servlet.ServletException expected) { originDenied = expected.getCause() instanceof io.dataease.ai.support.AiErrors.Failure; }
            if (!originDenied) throw new AssertionError("client cannot redirect a completed result into another conversation");
            var follow = json.readTree(chatMvc.perform(post("/ai/conversations/" + origin.path("sessionId").asText() + "/executions").contentType("application/json").content("{\"contextVersion\":\"" + origin.path("contextVersion").asText() + "\",\"text\":\"follow-background\"}")).andReturn().getResponse().getContentAsString());
            await(chatMvc, json, "/ai/conversations/" + origin.path("sessionId").asText() + "/executions/" + follow.path("executionId").asText() + "/events", "COMPLETED");
            if (!followed.get()) throw new AssertionError("follow-up model prompt contains original background request and legal result");
            chatMvc.perform(delete("/ai/conversations/" + origin.path("sessionId").asText()));
            boolean closedDenied = false;
            try { mvc.perform(post(linkedPath + "/conversation").contentType("application/json").content("{\"sessionId\":\"" + origin.path("sessionId").asText() + "\",\"contextVersion\":\"" + origin.path("contextVersion").asText() + "\"}")); }
            catch (jakarta.servlet.ServletException expected) { closedDenied = expected.getCause() instanceof io.dataease.ai.support.AiErrors.Failure; }
            if (!closedDenied) throw new AssertionError("closed conversation is never recreated from persisted task");
            toolsOn.set(true);
            var withTools = json.readTree(mvc.perform(post("/ai/tasks").contentType("application/json").content(request.replace("same-submit", "with-tools"))).andReturn().getResponse().getContentAsString());
            var toolResult = await(mvc, json, "/ai/tasks/" + withTools.path("taskId").asText(), "COMPLETED");
            if (executed.get() != 1 || !"durable answer".equals(toolResult.path("result").path("text").asText())) throw new AssertionError("trusted task executes actual authorized tool and correlated continuation");
            sceneAllowed.set(false);
            if (json.readTree(mvc.perform(get("/ai/tasks/notices")).andReturn().getResponse().getContentAsString()).size() != 0) throw new AssertionError("revoked resource notices are withheld");
            boolean deniedResult = false;
            try { mvc.perform(get("/ai/tasks/" + withTools.path("taskId").asText())); }
            catch (jakarta.servlet.ServletException expected) { deniedResult = expected.getCause() instanceof io.dataease.ai.support.AiErrors.Failure; }
            if (!deniedResult) throw new AssertionError("revoked resource cannot be read through task API");
            var cancelledCompleted = json.readTree(mvc.perform(post("/ai/tasks/" + withTools.path("taskId").asText() + "/cancel")).andReturn().getResponse().getContentAsString());
            if (!cancelledCompleted.path("result").isNull() || !cancelledCompleted.path("resourceName").isNull()) throw new AssertionError("cancellation cannot disclose a completed result or resource after READ revocation");
            sceneAllowed.set(true);
            // Explicit isolated expired sample: ordinary result/notice APIs must reject before cleanup runs.
            new org.springframework.transaction.support.TransactionTemplate(manager).executeWithoutResult(status ->
                    em.createQuery("update CoreAiTaskAttempt a set a.deliveryExpiresAt=:expired where a.id=:id")
                            .setParameter("expired", System.currentTimeMillis() - 1).setParameter("id", toolResult.path("attemptId").asLong()).executeUpdate());
            boolean expiredDenied = false;
            try { mvc.perform(get("/ai/tasks/" + withTools.path("taskId").asText())); }
            catch (jakarta.servlet.ServletException expected) { expiredDenied = expected.getCause() instanceof io.dataease.ai.support.AiErrors.Failure; }
            if (!expiredDenied) throw new AssertionError("expired business result rejected at read boundary before physical cleanup");
            var pendingNotices = json.readTree(mvc.perform(get("/ai/tasks/notices")).andReturn().getResponse().getContentAsString());
            for (var notice : pendingNotices) if (notice.path("attemptId").equals(toolResult.path("attemptId"))) throw new AssertionError("expired notice cannot be delivered");
            String safe = pendingNotices.toString();
            if (safe.contains("ownerJson") || safe.contains("fence") || safe.contains("explain") || safe.contains("durable answer")) throw new AssertionError("ordinary notices expose only safe delivery projection");
            var stateFailure = json.readTree(mvc.perform(post("/ai/tasks").contentType("application/json").content(request.replace("same-submit", "state-save-failure").replace("explain", "state-save-failure"))).andReturn().getResponse().getContentAsString());
            String statePath = "/ai/tasks/" + stateFailure.path("taskId").asText();
            for (int i = 0; i < 200 && !failCommit.get(); i++) Thread.sleep(25);
            if (!failCommit.get()) throw new AssertionError("controlled commit failure reached actual model completion");
            // Repeated worker terminal failures while multiple node heartbeats continue committing normally.
            Thread.sleep(2200);
            int sentBeforeRecovery = stateSends.get();
            var notSaved = json.readTree(mvc.perform(get(statePath)).andReturn().getResponse().getContentAsString());
            if ("COMPLETED".equals(notSaved.path("status").asText()) || !notSaved.path("result").isNull()) throw new AssertionError("uncommitted result must never be delivered as success");
            failCommit.set(false);
            await(mvc, json, statePath, "FAILED");
            if (stateSends.get() != sentBeforeRecovery) throw new AssertionError("terminal persistence recovery cannot rerun model work");
        } finally {
            failCommit.set(false);
            conversations.destroy(); tasks.destroy(); transport.close(); server.stop(0);
            var setting = configuration.setting();
            configuration.saveSettings(new io.dataease.api.ai.config.AiConfigDtos.SettingsChange(setting.getRowVersion(), true, previous, false, 60, 30000, 4096, 5, 2, configuration.impact("SETTINGS", previous + ":true").token()));
        }
        System.out.println("PASS task HTTP acceptance/idempotency, retry source binding, exact IDs, B pool/text/tools, revoked result/cancel isolation, repeated terminal commit failure recovery without model rerun");
    }
}
