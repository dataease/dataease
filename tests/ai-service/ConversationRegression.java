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
        var firstChunk = new AtomicReference<CountDownLatch>(new CountDownLatch(1));
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        var providerWorkers = Executors.newFixedThreadPool(4, task -> { Thread thread = new Thread(task, "synthetic-provider"); thread.setDaemon(true); return thread; });
        server.setExecutor(providerWorkers);
        server.createContext("/v1/chat/completions", exchange -> {
            var body = json.readTree(exchange.getRequestBody());
            synchronized (sent) { sent.add(body); }
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
                exchange.sendResponseHeaders("auth".equals(kind) ? 401 : "rate".equals(kind) ? 429 : 200, 0);
                try {
                    byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
                    if ("framed".equals(kind)) for (byte value : bytes) { exchange.getResponseBody().write(value); exchange.getResponseBody().flush(); }
                    else exchange.getResponseBody().write(bytes);
                } finally { exchange.close(); }
                return;
            }
            if (body.path("stream").asBoolean()) {
                exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
                exchange.sendResponseHeaders(200, 0);
                try {
                    if (beforeFirst.get()) { firstChunk.get().countDown(); streamRelease.get().await(20, TimeUnit.SECONDS); }
                    exchange.getResponseBody().write(("data: {\"choices\":[{\"index\":0,\"delta\":{\"content\":\"星河\"" + (reasoning.get() ? ",\"reasoning_content\":\"private-thought\"" : "") + "},\"finish_reason\":null}]}\n\n").getBytes(StandardCharsets.UTF_8));
                    exchange.getResponseBody().flush(); streamFirst.countDown(); firstChunk.get().countDown();
                    streamRelease.get().await(20, TimeUnit.SECONDS);
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
                public List<Resource> resources() { return List.of(new Resource("11", "Synthetic database")); }
                public Context resolve(String id, String sql) { return new Context(id, "Synthetic database", "v1", "SQL input: " + sql); }
                public void authorize(Context c) { java.util.concurrent.locks.LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(sceneLatencyMillis.get())); if (!allowed.get() || !Objects.equals(V3UserUtil.getUid(), 1L) || !"11".equals(c.resourceId())) throw AiErrors.error("forbidden"); }
            };
            var dialogueAccess = new AiAccess(null, null) {
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
            denyGet(mvc, configRun + "/events"); streamRelease.get().countDown();
            deny(mvc, configPath, configChanged.path("contextVersion").asText(), "Old configuration context retired");
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
