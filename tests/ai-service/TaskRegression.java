package io.dataease.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.ai.manage.*;
import io.dataease.ai.server.AiTaskServer;
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
            public List<io.dataease.ai.support.AiToolProtocol.Definition> tools(Context context) {
                return toolsOn.get() ? List.of(new io.dataease.ai.support.AiToolProtocol.Definition("inspect", "inspect authorized metadata", Map.of())) : List.of();
            }
            public String execute(Context context, String name, Map<String, String> arguments) {
                if (org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()) throw new AssertionError("tool I/O must not hold metadata transaction");
                authorize(context); executed.incrementAndGet(); return "{\"tables\":[\"authorized_table\"]}";
            }
        });
        var stateSends = new java.util.concurrent.atomic.AtomicInteger();
        var failCommit = new java.util.concurrent.atomic.AtomicBoolean();
        var failingManager = new org.springframework.orm.jpa.JpaTransactionManager(em.getEntityManagerFactory()) {
            @Override protected void doCommit(org.springframework.transaction.support.DefaultTransactionStatus status) {
                if (failCommit.get() && !Thread.currentThread().getName().equals("de-ai-task-heartbeat")) throw new org.springframework.transaction.TransactionSystemException("controlled unavailable commit");
                super.doCommit(status);
            }
        };
        failingManager.setRollbackOnCommitFailure(true);
        var tasks = new AiTaskManage(em, failingManager, beans.getBeanProvider(AiConversationScene.class),
                beans.getBeanProvider(AiRuntimeManage.class), access, new ObjectMapper());
        var mvc = MockMvcBuilders.standaloneSetup(new AiTaskServer(tasks)).build();
        String request = "{\"submissionKey\":\"same-submit\",\"scene\":\"sql\",\"capability\":\"sql.assist\",\"resourceId\":\"11\",\"editorId\":\"editor\",\"sql\":\"select 1\",\"text\":\"explain\"}";
        var first = mvc.perform(post("/ai/tasks").contentType("application/json").content(request)).andReturn().getResponse();
        if (first.getStatus() != 200) throw new AssertionError("task accepted");
        var json = new ObjectMapper();
        var a = json.readTree(first.getContentAsString());
        if (!a.path("taskId").isTextual() || !a.path("attemptId").isTextual()) throw new AssertionError("Snowflake IDs preserve exact browser precision");
        var b = json.readTree(mvc.perform(post("/ai/tasks").contentType("application/json").content(request)).andReturn().getResponse().getContentAsString());
        if (!a.path("taskId").equals(b.path("taskId")) || !"QUEUED".equals(a.path("status").asText())) throw new AssertionError("same submission returns the durable queued task");
        var read = json.readTree(mvc.perform(get("/ai/tasks/" + a.path("taskId").asText())).andReturn().getResponse().getContentAsString());
        if (!read.path("attemptId").equals(a.path("attemptId"))) throw new AssertionError("public task read identifies current attempt");
        String path = "/ai/tasks/" + a.path("taskId").asText();
        var cancelled = json.readTree(mvc.perform(post(path + "/cancel")).andReturn().getResponse().getContentAsString());
        if (!"CANCELLED".equals(cancelled.path("status").asText())) throw new AssertionError("queued cancellation is durable");
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
        for (int i = 0; i < 4; i++) {
            var accepted = json.readTree(mvc.perform(post("/ai/tasks").contentType("application/json").content(request.replace("same-submit", "bounded-" + i))).andReturn().getResponse().getContentAsString());
            bounded.add("/ai/tasks/" + accepted.path("taskId").asText());
        }
        if (json.readTree(mvc.perform(get("/ai/tasks")).andReturn().getResponse().getContentAsString()).size() != 4) throw new AssertionError("only current owner's bounded active tasks listed");
        boolean boundedReject = false;
        try { mvc.perform(post("/ai/tasks").contentType("application/json").content(request.replace("same-submit", "bounded-fifth"))); }
        catch (jakarta.servlet.ServletException expected) { boundedReject = expected.getCause() instanceof io.dataease.ai.support.AiErrors.Failure; }
        if (!boundedReject) throw new AssertionError("fifth active task rejected before unbounded admission");
        for (String boundedPath : bounded) mvc.perform(post(boundedPath + "/cancel"));
        var server = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            var modelRequest = json.readTree(exchange.getRequestBody());
            if (modelRequest.path("messages").toString().contains("state-save-failure")) { stateSends.incrementAndGet(); failCommit.set(true); }
            boolean needsTool = modelRequest.has("tools") && !modelRequest.path("messages").toString().contains("authorized_table");
            String toolFrame = "data: {\"choices\":[{\"index\":0,\"delta\":{\"tool_calls\":[{\"index\":0,\"id\":\"inspect-1\",\"type\":\"function\",\"function\":{\"name\":\"inspect\",\"arguments\":\"{}\"}}]},\"finish_reason\":\"tool_calls\"}]}\n\ndata: [DONE]\n\n";
            byte[] body = (needsTool ? toolFrame : ("data: {\"choices\":[{\"index\":0,\"delta\":{\"content\":\"durable answer\"},\"finish_reason\":\"stop\"}]}\n\ndata: [DONE]\n\n")).getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/event-stream"); exchange.sendResponseHeaders(200, body.length);
            try { exchange.getResponseBody().write(body); } finally { exchange.close(); }
        });
        server.start();
        var transport = new io.dataease.ai.support.AiTransport(new io.dataease.ai.support.AiOutboundPolicy(), admission, json);
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
            toolsOn.set(true);
            var withTools = json.readTree(mvc.perform(post("/ai/tasks").contentType("application/json").content(request.replace("same-submit", "with-tools"))).andReturn().getResponse().getContentAsString());
            var toolResult = await(mvc, json, "/ai/tasks/" + withTools.path("taskId").asText(), "COMPLETED");
            if (executed.get() != 1 || !"durable answer".equals(toolResult.path("result").path("text").asText())) throw new AssertionError("trusted task executes actual authorized tool and correlated continuation");
            sceneAllowed.set(false);
            boolean deniedResult = false;
            try { mvc.perform(get("/ai/tasks/" + withTools.path("taskId").asText())); }
            catch (jakarta.servlet.ServletException expected) { deniedResult = expected.getCause() instanceof io.dataease.ai.support.AiErrors.Failure; }
            if (!deniedResult) throw new AssertionError("revoked resource cannot be read through task API");
            var cancelledCompleted = json.readTree(mvc.perform(post("/ai/tasks/" + withTools.path("taskId").asText() + "/cancel")).andReturn().getResponse().getContentAsString());
            if (!cancelledCompleted.path("result").isNull() || !cancelledCompleted.path("resourceName").isNull()) throw new AssertionError("cancellation cannot disclose a completed result or resource after READ revocation");
            sceneAllowed.set(true);
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
            tasks.destroy(); transport.close(); server.stop(0);
            var setting = configuration.setting();
            configuration.saveSettings(new io.dataease.api.ai.config.AiConfigDtos.SettingsChange(setting.getRowVersion(), true, previous, false, 60, 30000, 4096, 5, 2, configuration.impact("SETTINGS", previous + ":true").token()));
        }
        System.out.println("PASS task HTTP acceptance/idempotency, retry source binding, exact IDs, B pool/text/tools, revoked result/cancel isolation, repeated terminal commit failure recovery without model rerun");
    }
}
