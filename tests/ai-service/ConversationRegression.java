package io.dataease.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import io.dataease.ai.dao.repository.CoreAiOrgPolicyRepository;
import io.dataease.ai.manage.*;
import io.dataease.ai.server.AiConversationServer;
import io.dataease.ai.support.*;
import io.dataease.api.ai.config.AiConfigDtos.*;
import io.dataease.api.ai.runtime.AiFeature;
import io.dataease.permission.util.V3UserUtil;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

/** Public dialogue HTTP + real controlled model HTTP; only the business scene is synthetic. */
public final class ConversationRegression {
    private static void deny(MockMvc mvc, String path, String version, String text) throws Exception {
        try {
            var response = mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsBytes(Map.of("contextVersion", version, "text", text))))
                    .andReturn().getResponse();
            if (response.getStatus() >= 400) return;
        } catch (jakarta.servlet.ServletException failure) {
            if (failure.getCause() instanceof AiErrors.Failure) return;
            throw failure;
        }
        throw new AssertionError("denied conversation returned a complete answer");
    }
    private static com.fasterxml.jackson.databind.JsonNode open(MockMvc mvc, ObjectMapper json, String editor) throws Exception {
        var result = mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsBytes(Map.of("scene", "sql", "resourceId", "11", "editorId", editor, "sql", "select 1"))))
                .andReturn().getResponse();
        if (result.getStatus() != 200) throw new AssertionError("temporary context capacity is available");
        return json.readTree(result.getContentAsString());
    }
    private static void denyGet(MockMvc mvc, String path) throws Exception {
        try {
            if (mvc.perform(get(path)).andReturn().getResponse().getStatus() >= 400) return;
        } catch (jakarta.servlet.ServletException failure) {
            if (failure.getCause() instanceof AiErrors.Failure) return;
            throw failure;
        }
        throw new AssertionError("revoked event request returned protected buffered output");
    }
    private static String chunk(String text, String finish) {
        return "data: {\"choices\":[{\"index\":0,\"delta\":{\"content\":\"" + text + "\"},\"finish_reason\":"
                + (finish == null ? "null" : "\"" + finish + "\"") + "}]}\n\n";
    }
    private static String start(MockMvc mvc, ObjectMapper json, String path, com.fasterxml.jackson.databind.JsonNode session, String text) throws Exception {
        var response = mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsBytes(Map.of("contextVersion", session.path("contextVersion").asText(), "text", text))))
                .andReturn().getResponse();
        if (response.getStatus() != 200) throw new AssertionError("execution start HTTP");
        return path + "/" + json.readTree(response.getContentAsString()).path("executionId").asText();
    }
    private static com.fasterxml.jackson.databind.JsonNode await(MockMvc mvc, ObjectMapper json, String path, String status, String content) throws Exception {
        com.fasterxml.jackson.databind.JsonNode result = null;
        for (int attempt = 0; attempt < 400; attempt++) {
            result = json.readTree(mvc.perform(get(path).param("after", "0")).andReturn().getResponse().getContentAsString());
            if (status.equals(result.path("status").asText()) && result.toString().contains(content)) return result;
            Thread.sleep(20);
        }
        throw new AssertionError("expected stream " + status + " containing " + content + ", got " + result);
    }
    private static void recovery(MockMvc mvc, ObjectMapper json, int step, int attempt) throws Exception {
        var current = (com.fasterxml.jackson.databind.node.ObjectNode) json.readTree(mvc.perform(get("/ai/settings")).andReturn().getResponse().getContentAsString());
        current.put("expectedVersion", current.path("rowVersion").asLong());
        current.set("recovery", json.valueToTree(Map.of("stepRetries", step, "attemptRetries", attempt, "initialDelayMillis", 1000)));
        if (mvc.perform(post("/ai/settings").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(current))).andReturn().getResponse().getStatus() != 200)
            throw new AssertionError("authorized recovery policy save");
    }
    private static String pathFor(com.fasterxml.jackson.databind.JsonNode session) {
        return "/ai/conversations/" + session.path("sessionId").asText() + "/turns";
    }
    public static void run(AiConfigurationManage configuration, CoreAiOrgPolicyRepository policies,
                           AiSecrets keys, AiAdmission admission, AiAccess access) throws Exception {
        var json = new ObjectMapper();
        var sent = new ArrayList<com.fasterxml.jackson.databind.JsonNode>();
        var allowed = new AtomicBoolean(true);
        var identityAllowed = new AtomicBoolean(true);
        var sceneLatencyMillis = new AtomicInteger();
        var revokeDuringCall = new AtomicBoolean();
        var streamFirst = new CountDownLatch(1);
        var streamFinish = new CountDownLatch(1);
        var streamRelease = new AtomicReference<CountDownLatch>(streamFinish);
        var truncated = new AtomicBoolean();
        var reasoning = new AtomicBoolean();
        var beforeFirst = new AtomicBoolean();
        var protocol = new AtomicReference<String>("normal");
        var temporaryFailures = new AtomicInteger();
        var unknownTool = new AtomicBoolean();
        var uncertainWrites = new AtomicInteger();
        var firstChunk = new AtomicReference<CountDownLatch>(new CountDownLatch(1));
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        var providerWorkers = Executors.newFixedThreadPool(4, task -> { Thread thread = new Thread(task, "synthetic-provider"); thread.setDaemon(true); return thread; });
        server.setExecutor(providerWorkers);
        server.createContext("/v1/chat/completions", exchange -> {
            var body = json.readTree(exchange.getRequestBody());
            synchronized (sent) { sent.add(body); }
            if (temporaryFailures.getAndUpdate(value -> Math.max(0, value - 1)) > 0) {
                exchange.sendResponseHeaders(503, -1); exchange.close(); return;
            }
            if (body.path("stream").asBoolean() && "private-unattended".equals(protocol.get())) {
                exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
                exchange.sendResponseHeaders(200, 0);
                try {
                    for (int frame = 0; frame < 400; frame++) {
                        exchange.getResponseBody().write(": heartbeat\n\ndata: {\"choices\":[{\"index\":0,\"delta\":{\"reasoning\":\"private-unattended\"}}]}\n\n".getBytes(StandardCharsets.UTF_8));
                        exchange.getResponseBody().flush();
                        Thread.sleep(100);
                    }
                } catch (InterruptedException failure) { Thread.currentThread().interrupt(); }
                finally { exchange.close(); }
                return;
            }
            if (body.path("stream").asBoolean() && !"normal".equals(protocol.get())) {
                String kind = protocol.get();
                String content = switch (kind) {
                    case "fast-private-burst" -> "data: {\"choices\":[{\"index\":0,\"delta\":{\"reasoning\":\"private-burst\"}}]}\n\n".repeat(120)
                            + chunk("字", null).repeat(80) + chunk("完成", "stop") + "data: [DONE]\n\n";
                    case "reasoning-alias" -> "data: {\"choices\":[{\"index\":0,\"delta\":{\"role\":\"assistant\",\"content\":\"\"}}]}\n\n"
                            + "data: {\"choices\":[{\"index\":0,\"delta\":{\"reasoning\":\"private-alias-thought\"}}]}\n\n"
                            + chunk("可见结果", "stop") + "data: [DONE]\n\n";
                    case "reasoning-invalid" -> "data: {\"choices\":[{\"index\":0,\"delta\":{\"reasoning\":{\"text\":\"private-alias-thought\"}}}]}\n\n"
                            + chunk("可见结果", "stop") + "data: [DONE]\n\n";
                    case "unknown-write" -> "data: {\"choices\":[{\"index\":0,\"delta\":{\"tool_calls\":[{\"index\":0,\"id\":\"uncertain\",\"type\":\"function\",\"function\":{\"name\":\"controlled_write\",\"arguments\":\"{}\"}}]},\"finish_reason\":\"tool_calls\"}]}\n\ndata: [DONE]\n\n";
                    case "balance-rate" -> "{\"error\":{\"code\":\"insufficient_quota\"}}";
                    case "json" -> "{\"choices\":[]}";
                    case "eof" -> chunk("partial", null);
                    case "done-only" -> "data: [DONE]\n\n";
                    case "length" -> chunk("partial", "length") + "data: [DONE]\n\n";
                    case "filter" -> chunk("partial", "content_filter") + "data: [DONE]\n\n";
                    case "unknown-finish" -> chunk("partial", "unknown") + "data: [DONE]\n\n";
                    case "trailing" -> chunk("partial", "stop").replace("}]}\n", "}]} {}\n") + "data: [DONE]\n\n";
                    case "malformed" -> "data: {invalid-secret-body}\n\n";
                    case "oversized" -> "data: " + "x".repeat(65537) + "\n\n";
                    case "tool" -> "data: {\"choices\":[{\"index\":0,\"delta\":{\"tool_calls\":[{\"function\":{\"name\":\"unknown\"}}]}}]}\n\n";
                    case "usage-limit" -> chunk("partial", "stop") + "data: {\"choices\":[],\"usage\":{\"completion_tokens\":5000}}\n\ndata: [DONE]\n\n";
                    case "output-limit" -> chunk("x".repeat(1000), null).repeat(70) + chunk("", "stop") + "data: [DONE]\n\n";
                    case "framed" -> ": comment\r\ndata: {\"choices\":[{\"index\":0,\"delta\":{\"role\":\"assistant\"}}]}\r\n\r\n"
                            + "data: {\"choices\":[{\"index\":0,\n"
                            + "data: \"delta\":{\"content\":\"多字节\"},\"finish_reason\":\"stop\"}]}\n\n"
                            + "data: {\"choices\":[],\"usage\":{\"completion_tokens\":3}}\n\ndata: [DONE]\n\n";
                    default -> "provider-secret-key";
                };
                exchange.getResponseHeaders().set("Content-Type", "json".equals(kind) ? "application/json" : "text/event-stream");
                exchange.sendResponseHeaders("auth".equals(kind) ? 401 : Set.of("rate", "balance-rate").contains(kind) ? 429 : 200, 0);
                try {
                    byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
                    if ("framed".equals(kind)) for (byte value : bytes) { exchange.getResponseBody().write(value); exchange.getResponseBody().flush(); }
                    else exchange.getResponseBody().write(bytes);
                } finally { exchange.close(); }
                return;
            }
            if (body.path("stream").asBoolean()) {
                var release = streamRelease.get();
                exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
                exchange.sendResponseHeaders(200, 0);
                try {
                    if (beforeFirst.get()) { firstChunk.get().countDown(); release.await(20, TimeUnit.SECONDS); }
                    exchange.getResponseBody().write(("data: {\"choices\":[{\"index\":0,\"delta\":{\"content\":\"星河\"" + (reasoning.get() ? ",\"reasoning_content\":\"private-thought\"" : "") + "},\"finish_reason\":null}]}\n\n").getBytes(StandardCharsets.UTF_8));
                    exchange.getResponseBody().flush(); streamFirst.countDown(); firstChunk.get().countDown();
                    release.await(20, TimeUnit.SECONDS);
                    exchange.getResponseBody().write("data: {\"choices\":[{\"index\":0,\"delta\":{\"content\":\"继续\"},\"finish_reason\":\"stop\"}]}\n\ndata: [DONE]\n\n".getBytes(StandardCharsets.UTF_8));
                } catch (InterruptedException failure) { Thread.currentThread().interrupt(); }
                finally { exchange.close(); }
                return;
            }
            if (revokeDuringCall.get()) allowed.set(false);
            byte[] bytes = ("{\"id\":\"synthetic\",\"object\":\"chat.completion\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"Remembered context\"},\"finish_reason\":\"" + (truncated.get() ? "length" : "stop") + "\"}]}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try { exchange.getResponseBody().write(bytes); } finally { exchange.close(); }
        });
        server.start();
        var transport = new AiTransport(new AiOutboundPolicy(), admission, json);
        Long previous = configuration.setting().getDefaultModelId();
        try {
            var draft = configuration.draft(); var candidate = new Candidate();
            candidate.setId(draft.id()); candidate.setDraftHandle(draft.draftHandle());
            candidate.setName("Dialogue synthetic"); candidate.setProvider("OPENAI_COMPATIBLE");
            candidate.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/v1");
            candidate.setModelIdentifier("general-chat"); candidate.setApiKey("synthetic-key");
            candidate.setTestProof(configuration.testProof(configuration.candidate(candidate, true)));
            var saved = configuration.save(candidate, true); var s = configuration.setting();
            configuration.saveSettings(new SettingsChange(s.getRowVersion(), true, saved.id(), false, 60, 30000, 4096, 5, 2, configuration.impact("SETTINGS", saved.id() + ":true").token()));
            AiConversationScene scene = new AiConversationScene() {
                public String code() { return "sql"; }
                public List<AiToolProtocol.Definition> tools() { return unknownTool.get() ? List.of(new AiToolProtocol.Definition("controlled_write", "Synthetic uncertain side effect, never business SQL", Map.of())) : List.of(); }
                public String execute(Context context, String name, Map<String, String> arguments) { uncertainWrites.incrementAndGet(); throw AiErrors.error("connection_failed"); }
                public List<Resource> resources() { return List.of(new Resource("11", "Synthetic database")); }
                public Context resolve(String id, String sql) { return new Context(id, "Synthetic database", "v1", "SQL input: " + sql); }
                public void authorize(Context c) { java.util.concurrent.locks.LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(sceneLatencyMillis.get())); if (!allowed.get() || !Objects.equals(V3UserUtil.getUid(), 1L) || !"11".equals(c.resourceId())) throw AiErrors.error("forbidden"); }
            };
            var dialogueAccess = new SyntheticAccess(null, null) {
                public AiDeploymentMode.State mode() { return access.mode(); }
                public Identity capture() {
                    Long proxy = V3UserUtil.getProxy().isProxy() ? V3UserUtil.getProxy().getProxyOid() : null;
                    return new Identity(V3UserUtil.getUid(), proxy, proxy, access.mode());
                }
                public void verify(Identity identity, boolean configuration) { if (!identityAllowed.get()) throw AiErrors.error("forbidden"); access.verify(identity, configuration); }
            };
            var beans = new StaticListableBeanFactory(); beans.addBean("scene", scene);
            var manage = new AiConversationManage(beans.getBeanProvider(AiConversationScene.class), beans.getBeanProvider(AiRuntimeManage.class), dialogueAccess);
            beans.addBean("conversation", manage);
            var runtime = new AiRuntimeManage(beans.getBeanProvider(AiFeature.class), configuration, policies, keys, transport, json, dialogueAccess);
            beans.addBean("runtime", runtime);
            var mvc = MockMvcBuilders.standaloneSetup(new AiConversationServer(manage)).build();
            var configMvc = MockMvcBuilders.standaloneSetup(new io.dataease.ai.server.AiConfigurationServer(configuration, null)).build();
            var recoverySettings = json.readTree(configMvc.perform(get("/ai/settings")).andReturn().getResponse().getContentAsString());
            if (recoverySettings.path("recovery").path("stepRetries").asInt(-1) != 2 || recoverySettings.path("recovery").path("attemptRetries").asInt(-1) != 3) throw new AssertionError("AI administrator exposes bounded recovery defaults");

            byte[] openOnce = json.writeValueAsBytes(Map.of("scene", "sql", "resourceId", "11", "editorId", "idempotent-editor", "sql", "select 1", "openKey", "open-once"));
            var openedOnce = json.readTree(mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON).content(openOnce)).andReturn().getResponse().getContentAsString());
            var reopened = json.readTree(mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON).content(openOnce)).andReturn().getResponse().getContentAsString());
            if (!openedOnce.path("sessionId").equals(reopened.path("sessionId"))) throw new AssertionError("lost Open response must reuse the same session and quota");
            byte[] submittedOnce = json.writeValueAsBytes(Map.of("contextVersion", openedOnce.path("contextVersion").asText(), "text", "Frozen question", "submissionKey", "submission-once"));
            String executionsOnce = "/ai/conversations/" + openedOnce.path("sessionId").asText() + "/executions";
            var runOnce = json.readTree(mvc.perform(post(executionsOnce).contentType(MediaType.APPLICATION_JSON).content(submittedOnce)).andReturn().getResponse().getContentAsString());
            var replayOnce = json.readTree(mvc.perform(post(executionsOnce).contentType(MediaType.APPLICATION_JSON).content(submittedOnce)).andReturn().getResponse().getContentAsString());
            if (!runOnce.path("executionId").equals(replayOnce.path("executionId")) || runOnce.path("taskId").asText().isBlank()) throw new AssertionError("lost accepted response must reuse the same task/attempt/execution");
            mvc.perform(delete("/ai/conversations/" + openedOnce.path("sessionId").asText()));
            boolean closedOpeningDenied = false;
            try { mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON).content(openOnce)); }
            catch (jakarta.servlet.ServletException expected) { closedOpeningDenied = expected.getCause() instanceof AiErrors.Failure known && "scope_changed".equals(known.safeCode()); }
            if (!closedOpeningDenied) throw new AssertionError("closed Open keys cannot recreate a context or consume new quota");

            protocol.set("framed");
            byte[] invokeOnce = json.writeValueAsBytes(Map.of("scene", "sql", "resourceId", "11", "editorId", "button-key", "sql", "select 1", "text", "One button task", "submissionKey", "button-once"));
            var invoked = json.readTree(mvc.perform(post("/ai/conversations/capabilities/sql.assist").contentType(MediaType.APPLICATION_JSON).content(invokeOnce)).andReturn().getResponse().getContentAsString());
            var invokedAgain = json.readTree(mvc.perform(post("/ai/conversations/capabilities/sql.assist").contentType(MediaType.APPLICATION_JSON).content(invokeOnce)).andReturn().getResponse().getContentAsString());
            if (!invoked.path("executionId").equals(invokedAgain.path("executionId"))) throw new AssertionError("unknown Invoke response replays the accepted task before opening a new session");
            mvc.perform(delete("/ai/conversations/" + invoked.path("sessionId").asText()));
            boolean closedInvokeDenied = false;
            try { mvc.perform(post("/ai/conversations/capabilities/sql.assist").contentType(MediaType.APPLICATION_JSON).content(invokeOnce)); }
            catch (jakarta.servlet.ServletException expected) { closedInvokeDenied = expected.getCause() instanceof AiErrors.Failure known && "scope_changed".equals(known.safeCode()); }
            if (!closedInvokeDenied) throw new AssertionError("closed button task keys cannot replay model work");
            var otherKeyScope = open(mvc, json, "other-key-context");
            boolean changedScopeDenied = false;
            try { mvc.perform(post("/ai/conversations/" + otherKeyScope.path("sessionId").asText() + "/executions").contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsBytes(Map.of("contextVersion", otherKeyScope.path("contextVersion").asText(), "text", "Frozen question", "submissionKey", "submission-once")))); }
            catch (jakarta.servlet.ServletException expected) { changedScopeDenied = expected.getCause() instanceof AiErrors.Failure known && "conflict".equals(known.safeCode()); }
            if (!changedScopeDenied) throw new AssertionError("a submission key cannot be rebound to another session/context");
            mvc.perform(delete("/ai/conversations/" + otherKeyScope.path("sessionId").asText()));
            protocol.set("eof");
            var retrySession = open(mvc, json, "retry-editor");
            String retryExecutions = "/ai/conversations/" + retrySession.path("sessionId").asText() + "/executions";
            String failedPath = start(mvc, json, retryExecutions, retrySession, "Original frozen task");
            var failedAttempt = await(mvc, json, failedPath + "/events", "FAILED", "partial");
            var alternateDraft = configuration.draft(); var alternate = new Candidate();
            alternate.setId(alternateDraft.id()); alternate.setDraftHandle(alternateDraft.draftHandle()); alternate.setName("New attempt model");
            alternate.setProvider("OPENAI_COMPATIBLE"); alternate.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/v1"); alternate.setModelIdentifier("new-attempt-model"); alternate.setApiKey("synthetic-new-key");
            alternate.setTestProof(configuration.testProof(configuration.candidate(alternate, true))); var alternateModel = configuration.save(alternate, true);
            var beforeNewModel = configuration.setting();
            configuration.saveSettings(new SettingsChange(beforeNewModel.getRowVersion(), true, alternateModel.id(), false, 60, 30000, 4096, 5, 2, configuration.impact("SETTINGS", alternateModel.id() + ":true").token()));
            await(mvc, json, failedPath + "/events", "FAILED", "partial");
            protocol.set("framed");
            String laterPath = start(mvc, json, retryExecutions, retrySession, "Later successful turn");
            await(mvc, json, laterPath + "/events", "COMPLETED", "多字节");
            byte[] retryBody = json.writeValueAsBytes(Map.of("retryKey", "retry-once", "expectedAttemptId", failedAttempt.path("attemptId").asText()));
            var retried = json.readTree(mvc.perform(post(failedPath + "/retry").contentType(MediaType.APPLICATION_JSON).content(retryBody)).andReturn().getResponse().getContentAsString());
            String retriedPath = retryExecutions + "/" + retried.path("executionId").asText();
            var recovered = await(mvc, json, retriedPath + "/events", "COMPLETED", "多字节");
            if (!failedAttempt.path("taskId").equals(recovered.path("taskId")) || recovered.path("attemptNumber").asInt() != 2
                    || !failedAttempt.path("attemptId").equals(recovered.path("sourceAttemptId"))) throw new AssertionError("manual retry preserves task and links an independent attempt");
            var frozen = sent.get(sent.size() - 1).path("messages");
            if (frozen.size() != 2 || !frozen.get(1).path("content").asText().equals("Original frozen task") || !sent.get(sent.size() - 1).path("model").asText().equals("new-attempt-model")) throw new AssertionError("retry must use first complete input snapshot, never later history");
            var replayRetry = json.readTree(mvc.perform(post(failedPath + "/retry").contentType(MediaType.APPLICATION_JSON).content(retryBody)).andReturn().getResponse().getContentAsString());
            if (!replayRetry.path("executionId").equals(retried.path("executionId"))) throw new AssertionError("retry response loss must not create another attempt");
            String followingPath = start(mvc, json, retryExecutions, retrySession, "After old retry");
            await(mvc, json, followingPath + "/events", "COMPLETED", "多字节");
            var following = sent.get(sent.size() - 1).path("messages");
            if (following.size() != 4 || !following.get(1).path("content").asText().equals("Later successful turn")) throw new AssertionError("old retry must not overwrite later successful history");
            var afterNewModel = configuration.setting();
            configuration.saveSettings(new SettingsChange(afterNewModel.getRowVersion(), true, saved.id(), false, 60, 30000, 4096, 5, 2, configuration.impact("SETTINGS", saved.id() + ":true").token()));
            mvc.perform(delete("/ai/conversations/" + retrySession.path("sessionId").asText()));
            protocol.set("framed"); temporaryFailures.set(1); sent.clear();
            var transientSession = open(mvc, json, "transient-editor");
            long retryStarted = System.currentTimeMillis();
            String transientPath = start(mvc, json, "/ai/conversations/" + transientSession.path("sessionId").asText() + "/executions", transientSession, "Retry temporary service failure");
            await(mvc, json, transientPath + "/events", "COMPLETED", "多字节");
            if (sent.size() != 2 || !sent.get(0).equals(sent.get(1)) || System.currentTimeMillis() - retryStarted < 1000) throw new AssertionError("temporary model failure retries original model/input after bounded wait");

            recovery(configMvc, json, 0, 0); sent.clear(); temporaryFailures.set(1);
            String disabledPath = start(mvc, json, "/ai/conversations/" + transientSession.path("sessionId").asText() + "/executions", transientSession, "Recovery disabled");
            await(mvc, json, disabledPath + "/events", "FAILED", "provider_unavailable");
            Thread.sleep(1150);
            if (sent.size() != 1) throw new AssertionError("zero policy disables technical model retries");
            recovery(configMvc, json, 2, 3); sent.clear(); temporaryFailures.set(10);
            String cancelWaitPath = start(mvc, json, "/ai/conversations/" + transientSession.path("sessionId").asText() + "/executions", transientSession, "Stop retry wait");
            for (int wait = 0; wait < 100 && sent.isEmpty(); wait++) Thread.sleep(10);
            mvc.perform(post(cancelWaitPath + "/stop")); Thread.sleep(1300);
            if (sent.size() != 1) throw new AssertionError("cancelled wait cannot send another model request");
            temporaryFailures.set(0);
            mvc.perform(delete("/ai/conversations/" + transientSession.path("sessionId").asText()));
            sent.clear(); protocol.set("unknown-write"); unknownTool.set(true);
            var unknownSession = open(mvc, json, "unknown-editor");
            String unknownPath = start(mvc, json, "/ai/conversations/" + unknownSession.path("sessionId").asText() + "/executions", unknownSession, "Controlled unknown result");
            var unknownResult = await(mvc, json, unknownPath + "/events", "FAILED", "outcome_unknown");
            if (unknownResult.path("retryEligible").asBoolean() || uncertainWrites.get() != 1) throw new AssertionError("unknown side effect must not be automatically or manually replayable");
            mvc.perform(delete("/ai/conversations/" + unknownSession.path("sessionId").asText()));
            unknownTool.set(false); protocol.set("balance-rate"); sent.clear();
            var balanceSession = open(mvc, json, "balance-editor");
            String balanceRun = start(mvc, json, "/ai/conversations/" + balanceSession.path("sessionId").asText() + "/executions", balanceSession, "Balance failures require human action");
            await(mvc, json, balanceRun + "/events", "FAILED", "provider_balance");
            if (sent.size() != 1) throw new AssertionError("429 insufficient balance must not be automatically retried as rate limiting");
            mvc.perform(delete("/ai/conversations/" + balanceSession.path("sessionId").asText()));
            // A finished one-shot result releases its slot, but a retry must atomically reacquire it.
            protocol.set("eof");
            var quotaButton = json.readTree(mvc.perform(post("/ai/conversations/capabilities/sql.assist").contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsBytes(Map.of("scene", "sql", "resourceId", "11", "editorId", "quota-button", "sql", "select 1", "text", "Safe failed one-shot"))))
                    .andReturn().getResponse().getContentAsString());
            String quotaButtonPath = "/ai/conversations/" + quotaButton.path("sessionId").asText() + "/executions/" + quotaButton.path("executionId").asText();
            var failedButton = await(mvc, json, quotaButtonPath + "/events", "FAILED", "partial");
            var occupied = new ArrayList<com.fasterxml.jackson.databind.JsonNode>();
            for (int slot = 0; slot < 4; slot++) occupied.add(open(mvc, json, "quota-active-" + slot));
            byte[] buttonRetry = json.writeValueAsBytes(Map.of("retryKey", "quota-retry", "expectedAttemptId", failedButton.path("attemptId").asText()));
            boolean quotaDenied = false;
            try { mvc.perform(post(quotaButtonPath + "/retry").contentType(MediaType.APPLICATION_JSON).content(buttonRetry)); }
            catch (jakarta.servlet.ServletException expected) { quotaDenied = expected.getCause() instanceof AiErrors.Failure known && "concurrency".equals(known.safeCode()); }
            if (!quotaDenied) throw new AssertionError("finished one-shot retry cannot launch a fifth active conversation");
            mvc.perform(delete("/ai/conversations/" + occupied.remove(0).path("sessionId").asText()));
            protocol.set("normal"); streamRelease.set(new CountDownLatch(1));
            var admittedButton = json.readTree(mvc.perform(post(quotaButtonPath + "/retry").contentType(MediaType.APPLICATION_JSON).content(buttonRetry)).andReturn().getResponse().getContentAsString());
            String admittedButtonPath = "/ai/conversations/" + admittedButton.path("sessionId").asText() + "/executions/" + admittedButton.path("executionId").asText();
            await(mvc, json, admittedButtonPath + "/events", "RUNNING", "星河");
            quotaDenied = false;
            try { open(mvc, json, "quota-overflow"); }
            catch (jakarta.servlet.ServletException expected) { quotaDenied = expected.getCause() instanceof AiErrors.Failure known && "concurrency".equals(known.safeCode()); }
            if (!quotaDenied) throw new AssertionError("running one-shot retry occupies a conversation slot");
            mvc.perform(post(admittedButtonPath + "/stop")); streamRelease.get().countDown();
            occupied.add(open(mvc, json, "quota-after-cancel"));
            for (var active : occupied) mvc.perform(delete("/ai/conversations/" + active.path("sessionId").asText()));
            mvc.perform(delete("/ai/conversations/" + quotaButton.path("sessionId").asText()));
            // A retired chat keeps its Task snapshot, while each running retry still occupies a slot.
            var retiredSource = open(mvc, json, "retired-quota");
            streamRelease.set(new CountDownLatch(1));
            String retiredRun = start(mvc, json, "/ai/conversations/" + retiredSource.path("sessionId").asText() + "/executions", retiredSource, "Frozen retired task");
            await(mvc, json, retiredRun + "/events", "RUNNING", "星河");
            var cancelledRetired = json.readTree(mvc.perform(post(retiredRun + "/stop")).andReturn().getResponse().getContentAsString());
            streamRelease.get().countDown();
            occupied.clear(); occupied.add(open(mvc, json, "retired-quota"));
            for (int slot = 1; slot < 4; slot++) occupied.add(open(mvc, json, "retired-active-" + slot));
            byte[] retiredRetry = json.writeValueAsBytes(Map.of("retryKey", "retired-retry", "expectedAttemptId", cancelledRetired.path("attemptId").asText()));
            quotaDenied = false;
            try { mvc.perform(post(retiredRun + "/retry").contentType(MediaType.APPLICATION_JSON).content(retiredRetry)); }
            catch (jakarta.servlet.ServletException expected) { quotaDenied = expected.getCause() instanceof AiErrors.Failure known && "concurrency".equals(known.safeCode()); }
            if (!quotaDenied) throw new AssertionError("retired task retry must reacquire the four-activity quota");
            mvc.perform(delete("/ai/conversations/" + occupied.remove(occupied.size() - 1).path("sessionId").asText()));
            streamRelease.set(new CountDownLatch(1));
            var admittedRetired = json.readTree(mvc.perform(post(retiredRun + "/retry").contentType(MediaType.APPLICATION_JSON).content(retiredRetry)).andReturn().getResponse().getContentAsString());
            String admittedRetiredPath = "/ai/conversations/" + admittedRetired.path("sessionId").asText() + "/executions/" + admittedRetired.path("executionId").asText();
            await(mvc, json, admittedRetiredPath + "/events", "RUNNING", "星河");
            quotaDenied = false;
            try { open(mvc, json, "retired-overflow"); }
            catch (jakarta.servlet.ServletException expected) { quotaDenied = expected.getCause() instanceof AiErrors.Failure known && "concurrency".equals(known.safeCode()); }
            if (!quotaDenied) throw new AssertionError("running retired task retry occupies the fourth activity slot");
            var stoppedRetired = json.readTree(mvc.perform(post(admittedRetiredPath + "/stop")).andReturn().getResponse().getContentAsString());
            streamRelease.get().countDown();
            // Open and a released Task retry compete for the final slot under the same atomic boundary.
            streamRelease.set(new CountDownLatch(1));
            var quotaRace = Executors.newFixedThreadPool(2);
            var readyQuotaRace = new CountDownLatch(2); var beginQuotaRace = new CountDownLatch(1);
            try {
                var requests = List.of(
                        post(admittedRetiredPath + "/retry").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(Map.of("retryKey", "retired-race", "expectedAttemptId", stoppedRetired.path("attemptId").asText()))),
                        post("/ai/conversations").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(Map.of("scene", "sql", "resourceId", "11", "editorId", "competing-open", "sql", "select 1"))));
                var outcomes = new ArrayList<Future<com.fasterxml.jackson.databind.JsonNode>>();
                for (var request : requests) outcomes.add(quotaRace.submit(() -> {
                    V3UserUtil.setUid(1L); readyQuotaRace.countDown();
                    try {
                        if (!beginQuotaRace.await(5, TimeUnit.SECONDS)) throw new AssertionError("quota race starts together");
                        return json.readTree(mvc.perform(request).andReturn().getResponse().getContentAsString());
                    } catch (jakarta.servlet.ServletException expected) {
                        if (expected.getCause() instanceof AiErrors.Failure known && "concurrency".equals(known.safeCode())) return null;
                        throw expected;
                    } finally { V3UserUtil.clear(); }
                }));
                if (!readyQuotaRace.await(5, TimeUnit.SECONDS)) throw new AssertionError("quota contenders ready");
                beginQuotaRace.countDown();
                var retryOutcome = outcomes.get(0).get(10, TimeUnit.SECONDS);
                var openOutcome = outcomes.get(1).get(10, TimeUnit.SECONDS);
                if ((retryOutcome == null) == (openOutcome == null)) throw new AssertionError("exactly one concurrent Open/retry acquires the fourth activity slot");
                if (retryOutcome != null) mvc.perform(post("/ai/conversations/" + retryOutcome.path("sessionId").asText() + "/executions/" + retryOutcome.path("executionId").asText() + "/stop"));
                if (openOutcome != null) mvc.perform(delete("/ai/conversations/" + openOutcome.path("sessionId").asText()));
            } finally { beginQuotaRace.countDown(); streamRelease.get().countDown(); quotaRace.shutdownNow(); }
            occupied.add(open(mvc, json, "retired-after-cancel"));
            int beforeRetiredTurn = sent.size();
            deny(mvc, pathFor(retiredSource), retiredSource.path("contextVersion").asText(), "Legacy retired context must not call model");
            if (sent.size() != beforeRetiredTurn) throw new AssertionError("legacy retired context is rejected before any model HTTP request");
            for (var active : occupied) mvc.perform(delete("/ai/conversations/" + active.path("sessionId").asText()));
            mvc.perform(delete("/ai/conversations/" + retiredSource.path("sessionId").asText()));
            streamRelease.set(streamFinish);
            protocol.set("framed");
            String lastEditedSession = null;
            for (int edit = 0; edit < 6; edit++) {
                var edited = json.readTree(mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("scene", "sql", "resourceId", "11", "editorId", "continuously-edited", "sql", "select " + edit)))).andReturn().getResponse().getContentAsString());
                lastEditedSession = edited.path("sessionId").asText();
                String editedRun = start(mvc, json, "/ai/conversations/" + edited.path("sessionId").asText() + "/executions", edited, "Explain edited SQL");
                await(mvc, json, editedRun + "/events", "COMPLETED", "多字节");
            }
            mvc.perform(delete("/ai/conversations/" + lastEditedSession));
            sent.clear(); protocol.set("normal");
            System.out.println("PASS public foreground Open/submit idempotency, frozen manual retry, atomic activity quota and temporary model recovery");
            if (Boolean.getBoolean("ai.test.recoveryOnly")) { manage.destroy(); return; }
            var open = mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"scene\":\"sql\",\"resourceId\":\"11\",\"editorId\":\"editor-a\",\"sql\":\"select 1\"}"))
                    .andReturn().getResponse();
            if (open.getStatus() != 200) throw new AssertionError("conversation opens through HTTP");
            var session = json.readTree(open.getContentAsString());
            String path = "/ai/conversations/" + session.path("sessionId").asText() + "/turns";
            String version = session.path("contextVersion").asText();
            for (String question : List.of("First question", "Follow up")) {
                var answer = mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("contextVersion", version, "text", question))))
                        .andReturn().getResponse();
                if (answer.getStatus() != 200 || !answer.getContentAsString().contains("Remembered context")) throw new AssertionError("dialogue HTTP response");
            }
            if (sent.size() != 2 || sent.get(1).path("messages").size() != 4
                    || !sent.get(1).path("messages").get(2).path("content").asText().equals("Remembered context")
                    || !sent.get(1).path("tools").isMissingNode() && !sent.get(1).path("tools").isEmpty()) throw new AssertionError("follow-up carries trusted context and completed history without tools");
            // Final aggregate input includes the existing conversation; a failed turn is never remembered.
            int before = sent.size();
            deny(mvc, path, version, "x".repeat(29990));
            deny(mvc, path, "forged-version", "forged context");
            mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsBytes(Map.of("contextVersion", version, "text", "Valid after rejected turn"))));
            if (sent.size() != before + 1 || sent.get(sent.size() - 1).path("messages").size() != 6)
                throw new AssertionError("failed turn entered completed history");
            before = sent.size();
            V3UserUtil.setUid(2L);
            deny(mvc, path, version, "another user");
            V3UserUtil.clear(); V3UserUtil.setUid(1L); V3UserUtil.setProxy(9001L);
            deny(mvc, path, version, "another workspace");
            V3UserUtil.clear(); V3UserUtil.setUid(1L);
            if (sent.size() != before) throw new AssertionError("denial or budget reached upstream");
            allowed.set(false); deny(mvc, path, version, "revoked"); allowed.set(true);
            deny(mvc, path, version, "revoked history cannot resume");
            if (sent.size() != before) throw new AssertionError("revoked history reused");
            var second = mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"scene\":\"sql\",\"resourceId\":\"11\",\"editorId\":\"editor-b\",\"sql\":\"select 2\"}"))
                    .andReturn().getResponse();
            var other = json.readTree(second.getContentAsString());
            String otherPath = "/ai/conversations/" + other.path("sessionId").asText() + "/turns";
            String otherVersion = other.path("contextVersion").asText();
            mvc.perform(post(otherPath).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(Map.of("contextVersion", otherVersion, "text", "Independent editor"))));
            if (sent.get(sent.size() - 1).path("messages").size() != 2) throw new AssertionError("editors share history");
            revokeDuringCall.set(true);
            deny(mvc, otherPath, otherVersion, "Revoke before delivery");
            allowed.set(true); revokeDuringCall.set(false);
            deny(mvc, otherPath, otherVersion, "late history cannot resume");
            // Closing a context removes its history and prevents later delivery/reuse.
            var third = mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"scene\":\"sql\",\"resourceId\":\"11\",\"editorId\":\"editor-c\",\"sql\":\"select 3\"}"))
                    .andReturn().getResponse();
            var closed = json.readTree(third.getContentAsString());
            String closedId = closed.path("sessionId").asText();
            if (mvc.perform(delete("/ai/conversations/" + closedId)).andReturn().getResponse().getStatus() != 200)
                throw new AssertionError("context close HTTP");
            deny(mvc, "/ai/conversations/" + closedId + "/turns", closed.path("contextVersion").asText(), "closed history");
            var streamingOpen = mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"scene\":\"sql\",\"resourceId\":\"11\",\"editorId\":\"stream\",\"sql\":\"select 1\"}"))
                    .andReturn().getResponse();
            var streamingSession = json.readTree(streamingOpen.getContentAsString());
            String streamingPath = "/ai/conversations/" + streamingSession.path("sessionId").asText() + "/executions";
            var started = mvc.perform(post(streamingPath).contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsBytes(Map.of("contextVersion", streamingSession.path("contextVersion").asText(), "text", "Stream this"))))
                    .andReturn().getResponse();
            if (started.getStatus() != 200) throw new AssertionError("streaming execution starts through dialogue HTTP");
            var run = json.readTree(started.getContentAsString());
            String eventsPath = streamingPath + "/" + run.path("executionId").asText() + "/events";
            if (!streamFirst.await(5, TimeUnit.SECONDS)) throw new AssertionError("real upstream stream requested");
            com.fasterxml.jackson.databind.JsonNode events = null;
            for (int attempt = 0; attempt < 100; attempt++) {
                events = json.readTree(mvc.perform(get(eventsPath).param("after", "0")).andReturn().getResponse().getContentAsString());
                if (events.toString().contains("星河")) break;
                Thread.sleep(20);
            }
            if (events == null || !events.toString().contains("星河") || !"RUNNING".equals(events.path("status").asText()))
                throw new AssertionError("first actual upstream increment is delivered while upstream remains blocked");
            // Execution IDs are not bearer credentials; another actor cannot read or stop this stream.
            V3UserUtil.clear(); V3UserUtil.setUid(2L);
            denyGet(mvc, eventsPath); deny(mvc, streamingPath + "/" + run.path("executionId").asText() + "/stop", version, "forged stop");
            V3UserUtil.clear(); V3UserUtil.setUid(1L);
            deny(mvc, streamingPath, streamingSession.path("contextVersion").asText(), "Concurrent turn on same context");
            streamFinish.countDown();
            for (int attempt = 0; attempt < 100; attempt++) {
                events = json.readTree(mvc.perform(get(eventsPath).param("after", "0")).andReturn().getResponse().getContentAsString());
                if (!"RUNNING".equals(events.path("status").asText())) break;
                Thread.sleep(20);
            }
            if (!"COMPLETED".equals(events.path("status").asText()) || !events.toString().contains("继续"))
                throw new AssertionError("stream completes with both actual upstream increments");
            // Stop retains the context and completed history, excludes partial output, and frees interactive admission.
            var currentSettings = configuration.setting();
            configuration.saveSettings(new SettingsChange(currentSettings.getRowVersion(), true, saved.id(), false, 60, 30000, 4096, 1, 1,
                    configuration.impact("SETTINGS", saved.id() + ":true").token()));
            streamRelease.set(new CountDownLatch(1));
            String stopRun = start(mvc, json, streamingPath, streamingSession, "Cancelled partial turn");
            await(mvc, json, stopRun + "/events", "RUNNING", "星河");
            // Another editor cannot acquire the occupied interactive slot; stream must retain it through its full body.
            var competing = json.readTree(mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"scene\":\"sql\",\"resourceId\":\"11\",\"editorId\":\"competitor\",\"sql\":\"select 1\"}"))
                    .andReturn().getResponse().getContentAsString());
            String competingPath = "/ai/conversations/" + competing.path("sessionId").asText() + "/executions";
            String rejectedRun = start(mvc, json, competingPath, competing, "Must be limited");
            await(mvc, json, rejectedRun + "/events", "FAILED", "concurrency");
            var cancelled = json.readTree(mvc.perform(post(stopRun + "/stop")).andReturn().getResponse().getContentAsString());
            if (!"CANCELLED".equals(cancelled.path("status").asText())) throw new AssertionError("stop publishes CANCELLED");
            streamRelease.get().countDown();
            String resumed = start(mvc, json, streamingPath, streamingSession, "Continue after stop");
            await(mvc, json, resumed + "/events", "COMPLETED", "继续");
            var followup = sent.get(sent.size() - 1).path("messages").toString();
            if (followup.contains("Cancelled partial turn") || !followup.contains("Stream this") || !followup.contains("星河继续"))
                throw new AssertionError("cancelled partial turn polluted completed model history");
            var stoppedAgain = json.readTree(mvc.perform(post(stopRun + "/stop")).andReturn().getResponse().getContentAsString());
            if (!"CANCELLED".equals(stoppedAgain.path("status").asText()) || stoppedAgain.path("events").size() != 1)
                throw new AssertionError("terminal cancellation is idempotent and does not publish late completion");
            mvc.perform(delete("/ai/conversations/" + competing.path("sessionId").asText()));
            // The native SDK normalizes finish reasons; a truncated synchronous reply must not be completed/history.
            truncated.set(true);
            deny(mvc, pathFor(streamingSession), streamingSession.path("contextVersion").asText(), "Truncated synchronous reply");
            truncated.set(false);
            var timeoutSettings = configuration.setting();
            configuration.saveSettings(new SettingsChange(timeoutSettings.getRowVersion(), true, saved.id(), false, 5, 30000, 4096, 1, 1,
                    configuration.impact("SETTINGS", saved.id() + ":true").token()));
            streamRelease.set(new CountDownLatch(1));
            String timeoutRun = start(mvc, json, streamingPath, streamingSession, "Finite whole-turn timeout");
            await(mvc, json, timeoutRun + "/events", "FAILED", "timeout");
            streamRelease.get().countDown();
            var normalSettings = configuration.setting();
            configuration.saveSettings(new SettingsChange(normalSettings.getRowVersion(), true, saved.id(), false, 60, 30000, 4096, 1, 1,
                    configuration.impact("SETTINGS", saved.id() + ":true").token()));
            // Fast supplier packetization must make progress within the configured deadline even with a slow external READ boundary.
            var burstSettings = configuration.setting();
            configuration.saveSettings(new SettingsChange(burstSettings.getRowVersion(), true, saved.id(), false, 5, 30000, 4096, 1, 1,
                    configuration.impact("SETTINGS", saved.id() + ":true").token()));
            sceneLatencyMillis.set(40); protocol.set("fast-private-burst");
            String burstRun = start(mvc, json, streamingPath, streamingSession, "Read a fast private/token burst within the fixed deadline");
            var burstEvents = await(mvc, json, burstRun + "/events", "COMPLETED", "完成");
            String burstText = java.util.stream.StreamSupport.stream(burstEvents.path("events").spliterator(), false)
                    .filter(event -> "TEXT".equals(event.path("type").asText())).map(event -> event.path("text").asText()).collect(java.util.stream.Collectors.joining());
            if (!burstText.equals("字".repeat(80) + "完成") || burstEvents.toString().contains("private-burst"))
                throw new AssertionError("fast supplier burst lost visible progress or disclosed private content");
            sceneLatencyMillis.set(0); protocol.set("normal");
            var restoredSettings = configuration.setting();
            configuration.saveSettings(new SettingsChange(restoredSettings.getRowVersion(), true, saved.id(), false, 60, 30000, 4096, 1, 1,
                    configuration.impact("SETTINGS", saved.id() + ":true").token()));
            reasoning.set(true);
            String privateReasoning = start(mvc, json, streamingPath, streamingSession, "Only visible content");
            var visible = await(mvc, json, privateReasoning + "/events", "COMPLETED", "星河");
            if (visible.toString().contains("private-thought")) throw new AssertionError("provider private reasoning was exposed");
            reasoning.set(false);
            protocol.set("reasoning-alias");
            String aliasRun = start(mvc, json, streamingPath, streamingSession, "Private reasoning alias stays private");
            var aliasEvents = await(mvc, json, aliasRun + "/events", "COMPLETED", "可见结果");
            if (aliasEvents.toString().contains("private-alias-thought") || aliasEvents.toString().contains("reasoning"))
                throw new AssertionError("private reasoning alias reached browser event delivery");
            protocol.set("normal");
            String aliasFollowup = start(mvc, json, streamingPath, streamingSession, "Visible follow-up after alias");
            await(mvc, json, aliasFollowup + "/events", "COMPLETED", "继续");
            var aliasHistory = sent.get(sent.size() - 1).path("messages").toString();
            if (aliasHistory.contains("private-alias-thought") || !aliasHistory.contains("可见结果"))
                throw new AssertionError("private reasoning alias entered completed model history");
            protocol.set("reasoning-invalid");
            String invalidAlias = start(mvc, json, streamingPath, streamingSession, "Reject structured private reasoning alias");
            var invalidAliasEvents = await(mvc, json, invalidAlias + "/events", "FAILED", "response_invalid");
            if (invalidAliasEvents.toString().contains("private-alias-thought")) throw new AssertionError("invalid reasoning alias leaked");
            for (String invalid : List.of("json", "eof", "done-only", "length", "filter", "unknown-finish", "malformed", "oversized", "tool", "usage-limit", "output-limit", "auth", "rate", "trailing")) {
                protocol.set(invalid);
                String failed = start(mvc, json, streamingPath, streamingSession, "Invalid protocol " + invalid);
                var failure = await(mvc, json, failed + "/events", "FAILED", "FAILED");
                if (failure.toString().contains("secret")) throw new AssertionError("provider secret leaked through safe events");
            }
            protocol.set("framed");
            String framed = start(mvc, json, streamingPath, streamingSession, "Framed UTF-8");
            await(mvc, json, framed + "/events", "COMPLETED", "多字节");
            protocol.set("normal");
            firstChunk.set(new CountDownLatch(1)); streamRelease.set(new CountDownLatch(1));
            String revoked = start(mvc, json, streamingPath, streamingSession, "Identity revoked before delivery");
            if (!firstChunk.get().await(5, TimeUnit.SECONDS)) throw new AssertionError("revocation stream reached upstream");
            identityAllowed.set(false);
            denyGet(mvc, revoked + "/events");
            identityAllowed.set(true); streamRelease.get().countDown();
            deny(mvc, streamingPath, streamingSession.path("contextVersion").asText(), "Revoked identity history must never resume");
            // Resource revocation also suppresses buffered output and permanently retires that context.
            var resourceRevoked = open(mvc, json, "resource-revoked");
            String resourcePath = "/ai/conversations/" + resourceRevoked.path("sessionId").asText() + "/executions";
            firstChunk.set(new CountDownLatch(1)); streamRelease.set(new CountDownLatch(1));
            String resourceRun = start(mvc, json, resourcePath, resourceRevoked, "Resource READ revoked");
            if (!firstChunk.get().await(5, TimeUnit.SECONDS)) throw new AssertionError("resource revocation upstream started");
            allowed.set(false); denyGet(mvc, resourceRun + "/events"); allowed.set(true); streamRelease.get().countDown();
            deny(mvc, resourcePath, resourceRevoked.path("contextVersion").asText(), "Restored READ cannot revive history");
            // Current settings revisions are checked again before buffered delivery, not only before model send.
            var configChanged = open(mvc, json, "config-changed");
            String configPath = "/ai/conversations/" + configChanged.path("sessionId").asText() + "/executions";
            firstChunk.set(new CountDownLatch(1)); streamRelease.set(new CountDownLatch(1));
            String configRun = start(mvc, json, configPath, configChanged, "Configuration changes before delivery");
            if (!firstChunk.get().await(5, TimeUnit.SECONDS)) throw new AssertionError("configuration upstream started");
            var revised = configuration.setting();
            configuration.saveSettings(new SettingsChange(revised.getRowVersion(), true, saved.id(), false, 60, 30000, 4096, 1, 1,
                    configuration.impact("SETTINGS", saved.id() + ":true").token()));
            streamRelease.get().countDown();
            await(mvc, json, configRun + "/events", "COMPLETED", "继续");
            mvc.perform(delete("/ai/conversations/" + configChanged.path("sessionId").asText()));
            // Closing an active context cancels its stream; the released slot is usable by a fresh editor.
            var activeClose = open(mvc, json, "close-active");
            String closePath = "/ai/conversations/" + activeClose.path("sessionId").asText() + "/executions";
            firstChunk.set(new CountDownLatch(1)); streamRelease.set(new CountDownLatch(1));
            String closingRun = start(mvc, json, closePath, activeClose, "Close active editor");
            if (!firstChunk.get().await(5, TimeUnit.SECONDS)) throw new AssertionError("active close upstream started");
            mvc.perform(delete("/ai/conversations/" + activeClose.path("sessionId").asText()));
            denyGet(mvc, closingRun + "/events"); streamRelease.get().countDown();
            // Stop before the first model delta cancels an active read and releases its slot.
            var early = open(mvc, json, "early-stop");
            String earlyPath = "/ai/conversations/" + early.path("sessionId").asText() + "/executions";
            firstChunk.set(new CountDownLatch(1)); streamRelease.set(new CountDownLatch(1)); beforeFirst.set(true);
            String earlyRun = start(mvc, json, earlyPath, early, "Stop before first delta");
            if (!firstChunk.get().await(5, TimeUnit.SECONDS)) throw new AssertionError("pre-delta request reached provider");
            var earlyStop = json.readTree(mvc.perform(post(earlyRun + "/stop")).andReturn().getResponse().getContentAsString());
            if (!"CANCELLED".equals(earlyStop.path("status").asText()) || earlyStop.toString().contains("星河"))
                throw new AssertionError("pre-first-delta stop terminal");
            beforeFirst.set(false); streamRelease.get().countDown();
            String earlyNext = start(mvc, json, earlyPath, early, "Healthy after early stop");
            await(mvc, json, earlyNext + "/events", "COMPLETED", "星河");
            mvc.perform(delete("/ai/conversations/" + early.path("sessionId").asText()));
            // Cancel one active stream while an independent healthy stream retains its connection and lease.
            var parallelSettings = configuration.setting();
            configuration.saveSettings(new SettingsChange(parallelSettings.getRowVersion(), true, saved.id(), false, 60, 30000, 4096, 2, 1,
                    configuration.impact("SETTINGS", saved.id() + ":true").token()));
            var parallelA = open(mvc, json, "parallel-a"); var parallelB = open(mvc, json, "parallel-b");
            String parallelPathA = "/ai/conversations/" + parallelA.path("sessionId").asText() + "/executions";
            String parallelPathB = "/ai/conversations/" + parallelB.path("sessionId").asText() + "/executions";
            streamRelease.set(new CountDownLatch(1));
            String parallelRunA = start(mvc, json, parallelPathA, parallelA, "Cancel A");
            String parallelRunB = start(mvc, json, parallelPathB, parallelB, "Keep B");
            await(mvc, json, parallelRunA + "/events", "RUNNING", "星河");
            await(mvc, json, parallelRunB + "/events", "RUNNING", "星河");
            mvc.perform(post(parallelRunA + "/stop"));
            await(mvc, json, parallelRunB + "/events", "RUNNING", "星河");
            streamRelease.get().countDown();
            await(mvc, json, parallelRunB + "/events", "COMPLETED", "继续");
            mvc.perform(delete("/ai/conversations/" + parallelA.path("sessionId").asText()));
            mvc.perform(delete("/ai/conversations/" + parallelB.path("sessionId").asText()));
            var abandoned = open(mvc, json, "abandoned-editor");
            String abandonedPath = "/ai/conversations/" + abandoned.path("sessionId").asText() + "/executions";
            firstChunk.set(new CountDownLatch(1)); streamRelease.set(new CountDownLatch(1));
            String lost = start(mvc, json, abandonedPath, abandoned, "Client disappears without close or events");
            if (!firstChunk.get().await(5, TimeUnit.SECONDS)) throw new AssertionError("lost client upstream started");
            Thread.sleep(16200);
            denyGet(mvc, lost + "/events");
            streamRelease.get().countDown();
            var capacity = new ArrayList<com.fasterxml.jackson.databind.JsonNode>();
            for (int index = 0; index < 4; index++) capacity.add(open(mvc, json, "after-abandonment-" + index));
            for (var item : capacity) mvc.perform(delete("/ai/conversations/" + item.path("sessionId").asText()));
            // A continuously readable private/heartbeat stream must retire its whole abandoned context too.
            protocol.set("private-unattended");
            var privateAbandoned = open(mvc, json, "private-abandoned");
            String privateAbandonedPath = "/ai/conversations/" + privateAbandoned.path("sessionId").asText() + "/executions";
            String privateLost = start(mvc, json, privateAbandonedPath, privateAbandoned, "No client polls while private frames keep arriving");
            Thread.sleep(16200);
            denyGet(mvc, privateLost + "/events");
            capacity.clear();
            for (int index = 0; index < 4; index++) capacity.add(open(mvc, json, "after-private-abandonment-" + index));
            for (var item : capacity) mvc.perform(delete("/ai/conversations/" + item.path("sessionId").asText()));
            protocol.set("normal");
            var shutdown = open(mvc, json, "shutdown-editor");
            String shutdownPath = "/ai/conversations/" + shutdown.path("sessionId").asText() + "/executions";
            firstChunk.set(new CountDownLatch(1)); streamRelease.set(new CountDownLatch(1));
            String shutdownRun = start(mvc, json, shutdownPath, shutdown, "Shutdown while active");
            if (!firstChunk.get().await(5, TimeUnit.SECONDS)) throw new AssertionError("shutdown upstream started");
            manage.destroy(); streamRelease.get().countDown(); denyGet(mvc, shutdownRun + "/events");
            System.out.println("PASS conversation HTTP multiround/isolation/budget, true upstream increments, stop/admission/history, protocol/UTF-8/output limits, finite timeout, permission/config buffered-delivery revocation, close/shutdown and lost-client capacity recovery");
        } finally {
            var s = configuration.setting();
            configuration.saveSettings(new SettingsChange(s.getRowVersion(), true, previous, false, 60, 30000, 4096, 5, 2, configuration.impact("SETTINGS", previous + ":true").token()));
            streamFinish.countDown(); streamRelease.get().countDown(); transport.close(); server.stop(0); providerWorkers.shutdownNow();
        }
    }
}
