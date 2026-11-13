package io.dataease.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import io.dataease.ai.dao.repository.CoreAiOrgPolicyRepository;
import io.dataease.ai.manage.*;
import io.dataease.ai.server.AiConversationServer;
import io.dataease.ai.support.*;
import io.dataease.api.ai.config.AiConfigDtos.*;
import io.dataease.api.ai.runtime.AiFeature;
import jakarta.persistence.*;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/** Real public dialogue/model HTTP and isolated audit observations; synthetic business resource. */
public final class AuditRegression {
    public static void run(AiConfigurationManage configuration, CoreAiOrgPolicyRepository policies, AiSecrets keys,
            AiAdmission admission, AiAccess access, EntityManager em, PlatformTransactionManager manager,
            JdbcTemplate jdbc, EntityManagerFactory factory) throws Exception {
        var json = new ObjectMapper();
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        var mode = new java.util.concurrent.atomic.AtomicReference<String>("normal");
        var sends = new java.util.concurrent.atomic.AtomicInteger();
        var failures = new java.util.concurrent.atomic.AtomicInteger();
        var partialReady = new java.util.concurrent.CountDownLatch(1);
        var releasePartial = new java.util.concurrent.CountDownLatch(1);
        server.createContext("/v1/chat/completions", exchange -> {
            exchange.getRequestBody().readAllBytes(); sends.incrementAndGet();
            if (failures.getAndUpdate(value -> Math.max(0, value - 1)) > 0) {
                byte[] body = "{\"error\":{\"message\":\"retry-failure-body synthetic-audit-secret\"}}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json"); exchange.sendResponseHeaders(503, body.length);
                try { exchange.getResponseBody().write(body); } finally { exchange.close(); } return;
            }
            String body = "data: {\"choices\":[{\"index\":0,\"delta\":{\"reasoning\":\"internal-process\",\"content\":\"audit-answer\"},\"finish_reason\":\"stop\"}]}\n\ndata: [DONE]\n\n";
            if ("partial".equals(mode.get())) body = "data: {\"choices\":[{\"index\":0,\"delta\":{\"content\":\"unfinished-private\"},\"finish_reason\":null}]}\n\n";
            if ("usage".equals(mode.get())) body = body.replace("data: [DONE]", "data: {\"choices\":[],\"usage\":{\"prompt_tokens\":7,\"completion_tokens\":3,\"total_tokens\":10}}\n\ndata: [DONE]");
            if ("slow".equals(mode.get())) body = "data: {\"choices\":[{\"index\":0,\"delta\":{\"content\":\"cancelled-partial\"},\"finish_reason\":null}]}\n\n";
            if ("echo".equals(mode.get())) body = "data: {\"choices\":[{\"index\":0,\"delta\":{\"content\":\"hello synthetic-audit-\"},\"finish_reason\":null}]}\n\ndata: {\"choices\":[{\"index\":0,\"delta\":{\"content\":\"secret goodbye\"},\"finish_reason\":\"stop\"}]}\n\ndata: [DONE]\n\n";
            if ("invalid-json".equals(mode.get())) body = "{\"unexpected\":\"invalid-private-payload synthetic-audit-secret\"}";
            exchange.getResponseHeaders().set("Content-Type", "invalid-json".equals(mode.get()) ? "application/json" : "text/event-stream");
            exchange.sendResponseHeaders(200, 0);
            try {
                exchange.getResponseBody().write(body.getBytes(StandardCharsets.UTF_8)); exchange.getResponseBody().flush();
                if ("slow".equals(mode.get())) { partialReady.countDown(); try { releasePartial.await(5, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException stopped) { Thread.currentThread().interrupt(); } }
            } finally { exchange.close(); }
        });
        server.start();
        var transport = new AiTransport(new AiOutboundPolicy(), admission, json);
        try {
            var draft = configuration.draft(); var candidate = new Candidate();
            candidate.setId(draft.id()); candidate.setDraftHandle(draft.draftHandle()); candidate.setName("Audit fixture");
            candidate.setProvider("OPENAI_COMPATIBLE"); candidate.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/v1");
            candidate.setModelIdentifier("audit-model"); candidate.setApiKey("synthetic-audit-secret");
            candidate.setTestProof(configuration.testProof(configuration.candidate(candidate, true)));
            var model = configuration.save(candidate, true); var settings = configuration.setting();
            configuration.saveSettings(new SettingsChange(settings.getRowVersion(), true, model.id(), false, 60, 30000, 4096, 5, 2, configuration.impact("SETTINGS", model.id() + ":true").token()));
            AiConversationScene scene = new AiConversationScene() {
                public String code() { return "sql"; }
                public List<Resource> resources() { return List.of(new Resource("11", "Synthetic")); }
                public Context resolve(String id, String sql) { return new Context(id, "Synthetic", "v1", "trusted-context:" + sql); }
                public void authorize(Context context) { }
            };
            var beans = new StaticListableBeanFactory(); beans.addBean("scene", scene);
            var conversations = new AiConversationManage(beans.getBeanProvider(AiConversationScene.class), beans.getBeanProvider(AiRuntimeManage.class), access);
            beans.addBean("conversation", conversations);
            var decision = new java.util.concurrent.atomic.AtomicReference<Boolean>(true);
            var checkedTarget = new java.util.concurrent.atomic.AtomicReference<Long>();
            var auth = (io.dataease.api.permissions.auth.api.InteractiveAuthApi) java.lang.reflect.Proxy.newProxyInstance(AuditRegression.class.getClassLoader(),
                    new Class[]{io.dataease.api.permissions.auth.api.InteractiveAuthApi.class}, (proxy, method, args) -> {
                        if (method.getName().equals("checkAiAuditAccess")) { checkedTarget.set((Long) args[3]); return decision.get(); }
                        if (method.isDefault()) return java.lang.reflect.InvocationHandler.invokeDefault(proxy, method, args);
                        return null;
                    });
            var authorization = new StaticListableBeanFactory(); authorization.addBean("auth", auth);
            var auditAccess = new SyntheticAccess(authorization.getBeanProvider(io.dataease.api.permissions.auth.api.InteractiveAuthApi.class), null) {
                public Identity capture() { return access.capture(); }
                public void verify(Identity identity, boolean configurationAccess) { access.verify(identity, configurationAccess); }
                public AiDeploymentMode.State mode() { return access.mode(); }
            };
            var audit = new AiAuditManage(em, manager, auditAccess, json);
            var migration = new io.dataease.listener.sql.AiAuditInit(jdbc, factory, em, manager); migration.execute(); migration.execute();
            var runtime = new AiRuntimeManage(beans.getBeanProvider(AiFeature.class), configuration, policies, keys, transport, json, access, audit);
            beans.addBean("runtime", runtime);
            var mvc = MockMvcBuilders.standaloneSetup(new AiConversationServer(conversations), new io.dataease.ai.server.AiAuditServer(audit), new io.dataease.ai.server.AiConfigurationServer(configuration,null)).build();
            var retention = (com.fasterxml.jackson.databind.node.ObjectNode) json.readTree(mvc.perform(get("/ai/settings")).andReturn().getResponse().getContentAsString());
            retention.put("expectedVersion",retention.path("rowVersion").asLong()); retention.putObject("retention").put("auditDays",45).put("deliveryDays",7);
            if (mvc.perform(post("/ai/settings").contentType("application/json").content(json.writeValueAsBytes(retention))).andReturn().getResponse().getStatus() != 200) throw new AssertionError("configured audit retention through public settings API");
            var response = json.readTree(mvc.perform(post("/ai/conversations/capabilities/sql.assist").contentType("application/json")
                    .content("{\"scene\":\"sql\",\"resourceId\":\"11\",\"editorId\":\"audit-editor\",\"sql\":\"select 1\",\"text\":\"audit-question\"}")).andReturn().getResponse().getContentAsString());
            String execution = response.path("executionId").asText();
            for (int index = 0; index < 200; index++) {
                var events = json.readTree(mvc.perform(get("/ai/conversations/" + response.path("sessionId").asText() + "/executions/" + execution + "/events").param("after", "0")).andReturn().getResponse().getContentAsString());
                if ("COMPLETED".equals(events.path("status").asText())) break;
                Thread.sleep(20);
            }
            var detail = mvc.perform(get("/ai/audit/" + execution)).andReturn().getResponse();
            if (detail.getStatus() != 200) throw new AssertionError("administrator can inspect persisted actual execution audit via HTTP, got " + detail.getStatus());
            var evidence = json.readTree(detail.getContentAsString());
            if (evidence.path("retentionExpiresAt").asLong()-evidence.path("finishedAt").asLong() != java.util.concurrent.TimeUnit.DAYS.toMillis(45)) throw new AssertionError("actual model HTTP execution uses configured independent audit retention");
            if (!evidence.toString().contains("internal-process") || !evidence.toString().contains("audit-question") || !evidence.toString().contains("trusted-context:select 1"))
                throw new AssertionError("audit keeps actual complete input and vendor output, including process");
            if (evidence.toString().contains("synthetic-audit-secret")) throw new AssertionError("system credential entered audit");
            if (!"sql:11@v1".equals(evidence.path("resourceReference").asText())) throw new AssertionError("audit preserves actual authorized business resource, not a session lookup key");
            if (!evidence.path("taskId").equals(response.path("taskId")) || !evidence.path("attemptId").equals(response.path("attemptId"))
                    || evidence.path("entries").size() != 1 || !evidence.path("entries").get(0).path("usageJson").isNull()) throw new AssertionError("trusted attempt coordinates and unknown provider usage");
            mvc.perform(delete("/ai/conversations/" + response.path("sessionId").asText()));
            if (mvc.perform(get("/ai/audit/" + execution)).andReturn().getResponse().getStatus() != 200) throw new AssertionError("closing a chat preserves audit without recovering chat");
            var off = (com.fasterxml.jackson.databind.node.ObjectNode) json.readTree(mvc.perform(get("/ai/settings")).andReturn().getResponse().getContentAsString());
            off.put("expectedVersion", off.path("rowVersion").asLong()); off.put("enabled", false);
            off.put("impactToken", json.readTree(mvc.perform(get("/ai/settings/impact").param("defaultModelId", off.path("defaultModelId").asText()).param("enabled", "false")).andReturn().getResponse().getContentAsString()).path("token").asText());
            if (mvc.perform(post("/ai/settings").contentType("application/json").content(json.writeValueAsBytes(off))).andReturn().getResponse().getStatus() != 200) throw new AssertionError("public AI shutdown accepted");
            if (mvc.perform(get("/ai/audit/" + execution)).andReturn().getResponse().getStatus() != 200) throw new AssertionError("AI shutdown must preserve independent authorized administrator audit detail");
            var on = (com.fasterxml.jackson.databind.node.ObjectNode) json.readTree(mvc.perform(get("/ai/settings")).andReturn().getResponse().getContentAsString());
            on.put("expectedVersion", on.path("rowVersion").asLong()); on.put("enabled", true);
            if (mvc.perform(post("/ai/settings").contentType("application/json").content(json.writeValueAsBytes(on))).andReturn().getResponse().getStatus() != 200) throw new AssertionError("public AI reopen accepted");

            mode.set("partial");
            var partial = json.readTree(mvc.perform(post("/ai/conversations/capabilities/sql.assist").contentType("application/json")
                    .content("{\"scene\":\"sql\",\"resourceId\":\"11\",\"editorId\":\"audit-partial\",\"sql\":\"select 1\",\"text\":\"partial-question\"}")).andReturn().getResponse().getContentAsString());
            String partialId = partial.path("executionId").asText();
            for (int index = 0; index < 200; index++) {
                var progress = json.readTree(mvc.perform(get("/ai/conversations/" + partial.path("sessionId").asText() + "/executions/" + partialId + "/events").param("after", "0")).andReturn().getResponse().getContentAsString());
                if ("RUNNING".equals(progress.path("status").asText())) { Thread.sleep(20); continue; }
                var value = json.readTree(mvc.perform(get("/ai/audit/" + partialId)).andReturn().getResponse().getContentAsString());
                if ("FAILED".equals(value.path("status").asText())) {
                    if (!value.path("incomplete").asBoolean() || !value.toString().contains("unfinished-private") || !value.path("entries").get(0).path("incomplete").asBoolean()) throw new AssertionError("failed partial output must remain incomplete");
                    break;
                }
                if (index == 199) throw new AssertionError("failed partial audit never completed");
                Thread.sleep(20);
            }
            decision.set(null); denied(mvc, "/ai/audit/" + execution);
            decision.set(false); denied(mvc, "/ai/audit/" + execution);
            decision.set(true);
            if (checkedTarget.get() != null) throw new AssertionError("audit authorization uses persisted global scope");
            failures.set(1); mode.set("normal");
            var retry = invoke(mvc, json, "audit-retry"); await(mvc, json, retry, "COMPLETED");
            var retried = json.readTree(mvc.perform(get("/ai/audit/" + retry.path("executionId").asText())).andReturn().getResponse().getContentAsString());
            if (retried.path("entries").size() != 2 || !retried.path("entries").get(0).path("responseBody").asText().contains("retry-failure-body"))
                throw new AssertionError("each real technical resend preserves its intermediate upstream failure body");
            var first = retried.path("entries").get(0); var second = retried.path("entries").get(1);
            if (!first.path("stepId").equals(second.path("stepId")) || !first.path("id").equals(second.path("previousEntryId")) || retried.toString().contains("synthetic-audit-secret"))
                throw new AssertionError("technical sends share the logical step, predecessor and credential redaction");
            mode.set("echo");
            var echo = invoke(mvc, json, "audit-echo"); await(mvc, json, echo, "COMPLETED");
            var echoEvidence = json.readTree(mvc.perform(get("/ai/audit/" + echo.path("executionId").asText())).andReturn().getResponse().getContentAsString());
            var combined = new StringBuilder();
            for (String line : echoEvidence.path("entries").get(0).path("responseBody").asText().split("\n")) {
                if (line.startsWith("data: {") ) combined.append(json.readTree(line.substring(6)).path("choices").path(0).path("delta").path("content").asText());
            }
            if (combined.toString().contains("synthetic-audit-secret")) throw new AssertionError("provider echo split across frames must not expose the system key through audit");
            mode.set("slow");
            var cancelled = invoke(mvc, json, "audit-cancelled");
            if (!partialReady.await(3, java.util.concurrent.TimeUnit.SECONDS)) throw new AssertionError("cancel fixture produced no partial output");
            Thread.sleep(50);
            mvc.perform(delete("/ai/conversations/" + cancelled.path("sessionId").asText())); releasePartial.countDown();
            boolean capturedCancel = false;
            for (int index = 0; index < 200; index++) {
                var value = json.readTree(mvc.perform(get("/ai/audit/" + cancelled.path("executionId").asText())).andReturn().getResponse().getContentAsString());
                if (!value.path("entries").get(0).path("finishedAt").isNull()) {
                    if (!"CANCELLED".equals(value.path("status").asText()) || !value.path("incomplete").asBoolean() || !value.toString().contains("cancelled-partial")) throw new AssertionError("cancelled partial output remains persistently incomplete");
                    capturedCancel = true; break;
                }
                Thread.sleep(20);
            }
            if (!capturedCancel) throw new AssertionError("cancelled request audit never finished");
            mode.set("usage");
            var measured = invoke(mvc, json, "audit-usage"); await(mvc, json, measured, "COMPLETED");
            var measuredAudit = json.readTree(mvc.perform(get("/ai/audit/" + measured.path("executionId").asText())).andReturn().getResponse().getContentAsString());
            if (json.readTree(measuredAudit.path("entries").get(0).path("usageJson").asText()).path("total_tokens").asInt() != 10) throw new AssertionError("only actual provider usage is recorded");
            mode.set("invalid-json");
            var invalid = invoke(mvc, json, "audit-invalid-protocol"); await(mvc, json, invalid, "FAILED");
            var invalidAudit = json.readTree(mvc.perform(get("/ai/audit/" + invalid.path("executionId").asText())).andReturn().getResponse().getContentAsString());
            if (!invalidAudit.toString().contains("invalid-private-payload") || invalidAudit.toString().contains("synthetic-audit-secret")) throw new AssertionError("invalid upstream protocol body is preserved safely as incomplete evidence");
            mode.set("normal");
            jdbc.execute("drop table core_ai_audit_entry");
            var degraded = invoke(mvc, json, "audit-degraded"); await(mvc, json, degraded, "COMPLETED");
            migration.execute();
            boolean restored = false;
            for (int index = 0; index < 350; index++) {
                if (audit.pendingGapCount() == 0) {
                    var gap = json.readTree(mvc.perform(get("/ai/audit/" + degraded.path("executionId").asText())).andReturn().getResponse().getContentAsString());
                    if (!gap.path("incomplete").asBoolean() || gap.path("gapCount").asLong() <= 0 || !"COMPLETED".equals(gap.path("status").asText())) throw new AssertionError("recovered audit marks missing evidence while execution remains complete");
                    restored = true; break;
                }
                Thread.sleep(20);
            }
            if (!restored) throw new AssertionError("bounded gap metadata did not recover after audit store restored");
            audit.close();
            System.out.println("PASS AI public HTTP actual execution input/output, unknown usage, partial failure and persistent audit after chat close");
        } finally { releasePartial.countDown(); transport.close(); server.stop(0); }
    }
    private static com.fasterxml.jackson.databind.JsonNode invoke(org.springframework.test.web.servlet.MockMvc mvc, ObjectMapper json, String editor) throws Exception {
        return json.readTree(mvc.perform(post("/ai/conversations/capabilities/sql.assist").contentType("application/json")
                .content(json.writeValueAsBytes(Map.of("scene", "sql", "resourceId", "11", "editorId", editor, "sql", "select 1", "text", "audit-question"))))
                .andReturn().getResponse().getContentAsString());
    }
    private static void await(org.springframework.test.web.servlet.MockMvc mvc, ObjectMapper json, com.fasterxml.jackson.databind.JsonNode run, String status) throws Exception {
        for (int i = 0; i < 350; i++) {
            var events = json.readTree(mvc.perform(get("/ai/conversations/" + run.path("sessionId").asText() + "/executions/" + run.path("executionId").asText() + "/events").param("after", "0")).andReturn().getResponse().getContentAsString());
            if (status.equals(events.path("status").asText())) { mvc.perform(delete("/ai/conversations/" + run.path("sessionId").asText())); return; }
            Thread.sleep(20);
        }
        throw new AssertionError("execution did not reach " + status);
    }
    private static void denied(org.springframework.test.web.servlet.MockMvc mvc, String path) throws Exception {
        try { if (mvc.perform(get(path)).andReturn().getResponse().getStatus() >= 400) return; }
        catch (jakarta.servlet.ServletException failure) { if (failure.getCause() instanceof AiErrors.Failure) return; throw failure; }
        throw new AssertionError("null/false audit authorization must fail closed");
    }

}
