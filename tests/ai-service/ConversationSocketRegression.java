package io.dataease.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.ai.engine.conversation.AiConversationManage;
import io.dataease.ai.server.AiConversationServer;
import io.dataease.auth.interceptor.CorsConfig;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.websocket.WebSocketAuthService;
import io.dataease.websocket.config.WsConfig;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.websocket.server.WsSci;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.*;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** Real loopback editor HTTP, platform STOMP and the independently controlled upstream model. */
public final class ConversationSocketRegression {
    @Configuration
    @EnableWebMvc
    @EnableWebSocketMessageBroker
    static class WebConfiguration { }
    private static final ThreadLocal<Long> workspace = new ThreadLocal<>();
    static Long workspace() { return workspace.get(); }
    static final ObjectMapper json = new ObjectMapper();

    static class EditorHttp {
        private final HttpClient client = HttpClient.newHttpClient();

        HttpResponse<String> request(String method, String path, Object body, String user, String workspace) throws Exception {
            var builder = HttpRequest.newBuilder(URI.create(path)).header("X-Test-Identity", user)
                    .header("X-DE-Error-Contract", "2").header("Content-Type", "application/json");
            if (workspace != null) builder.header("X-Test-Scope", workspace);
            return client.send(builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofByteArray(json.writeValueAsBytes(body))).build(), HttpResponse.BodyHandlers.ofString());
        }

        JsonNode ok(String method, String path, Object body) throws Exception {
            var response = request(method, path, body, "writer", null);
            if (response.statusCode() != 200) throw new AssertionError(method + " " + path + " -> " + response.statusCode() + " " + response.body());
            return json.readTree(response.body());
        }

        String startTurn(String session, String version, String text) throws Exception {
            String path = session + "/executions";
            return path + "/" + ok("POST", path, Map.of("contextVersion", version, "text", text)).path("executionId").asText();
        }

        JsonNode finish(String run) throws Exception {
            for (int i = 0; i < 240; i++) {
                var events = ok("GET", run + "/events?after=0", null);
                if (!"RUNNING".equals(events.path("status").asText())) return events;
                Thread.sleep(20);
            }
            throw new AssertionError("no terminal event for " + run);
        }

        JsonNode ask(String base, JsonNode session, String text) throws Exception {
            return finish(startTurn(base + "/" + session.path("sessionId").asText(), session.path("contextVersion").asText(), text));
        }
    }

    static Long actor(String token) {
        return "writer".equals(token) ? 1L : "other".equals(token) ? 2L : null;
    }

    static JsonNode post(String url, Object body, boolean allowed) throws Exception {
        var request = HttpRequest.newBuilder(URI.create(url)).header("Content-Type", "application/json")
                .header("X-Test-Identity", "writer").POST(HttpRequest.BodyPublishers.ofByteArray(json.writeValueAsBytes(body))).build();
        var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (allowed != (response.statusCode() == 200))
            throw new AssertionError("unexpected HTTP " + response.statusCode() + " for " + URI.create(url).getPath());
        return allowed && !response.body().isEmpty() ? json.readTree(response.body()) : null;
    }

    private record Tab(StompSession socket, BlockingQueue<JsonNode> messages, String challenge) implements AutoCloseable {
        static Tab open(int port, WebSocketStompClient client, String actor) throws Exception {
            return open(port, client, actor, false);
        }
        static Tab open(int port, WebSocketStompClient client, String actor, boolean acknowledge) throws Exception {
            var socket = client.connectAsync("ws://127.0.0.1:" + port + "/websocket/websocket?token=" + actor,
                    new StompSessionHandlerAdapter() {}).get(8, TimeUnit.SECONDS);
            var messages = new LinkedBlockingQueue<JsonNode>();
            var binding = new java.util.concurrent.atomic.AtomicReference<String>();
            socket.subscribe("/app/ai/conversation", new StompFrameHandler() {
                public Type getPayloadType(StompHeaders headers) { return JsonNode.class; }
                public void handleFrame(StompHeaders headers, Object body) {
                    var frame = (JsonNode) body;
                    if (frame.has("subscription")) binding.set(frame.path("subscription").asText());
                    if (acknowledge && "RUNNING".equals(frame.path("status").asText()))
                        socket.send("/app/ai/conversation/received", Map.of("subscription", binding.get()));
                    messages.add(frame);
                }
            });
            var ready = messages.poll(5, TimeUnit.SECONDS);
            if (ready == null || !ready.hasNonNull("subscription")) throw new AssertionError("the originating tab must receive its private binding challenge");
            return new Tab(socket, messages, ready.path("subscription").asText());
        }
        public void close() { if (socket.isConnected()) socket.disconnect(); }
    }

    private record Accepted(String session, String execution, String editor, String version) { }
    private static Accepted start(String base, String editor, String question) throws Exception {
        var session = post(base, Map.of("scene", "dataset-sql", "resourceId", "11", "editorId", editor), true);
        String url = base + "/" + session.path("sessionId").asText();
        String version = session.path("contextVersion").asText();
        var run = post(url + "/executions", Map.of("contextVersion", version, "text", question), true);
        return new Accepted(url, url + "/executions/" + run.path("executionId").asText(), editor, version);
    }

    private static JsonNode read(Accepted run, long cursor) throws Exception {
        var request = HttpRequest.newBuilder(URI.create(run.execution + "/events?after=" + cursor))
                .header("X-Test-Identity", "writer").GET().build();
        var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new AssertionError("original execution must remain readable: " + response.statusCode());
        return json.readTree(response.body());
    }

    private static JsonNode bindAndRead(Accepted run, Tab tab, long after) throws Exception {
        post(run.execution + "/subscriptions", Map.of("subscription", tab.challenge, "editorId", run.editor, "after", after), true);
        for (;;) {
            var page = tab.messages.poll(5, TimeUnit.SECONDS);
            if (page == null) throw new AssertionError("missing original execution after reconnect");
            if (!page.path("events").isEmpty() || !"RUNNING".equals(page.path("status").asText())) return page;
        }
    }

    private static void close(Accepted run) throws Exception {
        var request = HttpRequest.newBuilder(URI.create(run.session)).header("X-Test-Identity", "writer").DELETE().build();
        if (HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.discarding()).statusCode() != 200)
            throw new AssertionError("editor close must retire its context");
    }

    private static void grace(String base, int port, WebSocketStompClient client, List<JsonNode> modelRequests,
                              java.util.concurrent.atomic.AtomicReference<CountDownLatch> release) throws Exception {
        int before = modelRequests.size();
        release.set(new CountDownLatch(1));
        Accepted expires = start(base, "lost-route", "hold beyond grace");
        Accepted resumes = start(base, "resumed-route", "hold beyond grace");
        Accepted live = start(base, "live-route", "hold beyond grace");
        long resumeAfter;
        try (var silent = Tab.open(port, client, "writer"); var liveTab = Tab.open(port, client, "writer", true)) {
            if (!bindAndRead(expires, silent, 0).toString().contains("星河")) throw new AssertionError("first partial answer");
            bindAndRead(live, liveTab, 0);
            try (var second = Tab.open(port, client, "writer")) {
                resumeAfter = bindAndRead(resumes, second, 0).path("cursor").asLong();
            }
            Thread.sleep(18000);
            if (!"RUNNING".equals(read(resumes, resumeAfter).path("status").asText())) throw new AssertionError("original run survives 18 seconds offline");
            Thread.sleep(13000);
            var interrupted = read(expires, 0);
            if (!"FAILED".equals(interrupted.path("status").asText()) || !interrupted.toString().contains("星河")
                    || !interrupted.toString().contains("connection_interrupted") || !interrupted.path("retryEligible").asBoolean())
                throw new AssertionError("sending to a silent socket cannot renew the grace period");
            if (!"RUNNING".equals(read(live, 0).path("status").asText()))
                throw new AssertionError("the original editor's receipts keep a healthy execution alive");
        }
        release.get().countDown();
        try (var restored = Tab.open(port, client, "writer")) {
            var page = bindAndRead(resumes, restored, resumeAfter);
            if (!"COMPLETED".equals(read(resumes, 0).path("status").asText())
                    || page.path("events").get(0).path("sequence").asLong() != resumeAfter + 1)
                throw new AssertionError("completed original run replays only unread events");
        }
        if (modelRequests.size() - before != 3) throw new AssertionError("three accepted questions produce exactly three model requests");
        close(expires); close(resumes); close(live);
        System.out.println("PASS real 18s reconnect, 31s silent socket interruption, healthy socket receipts, one model request per accepted question");
    }

    private static void crossNode(String base, int origin, WebSocketStompClient client, List<JsonNode> modelRequests,
                                  java.util.concurrent.atomic.AtomicReference<CountDownLatch> release) throws Exception {
        Path ready = Files.createTempFile("ai-recovery-peer", ".port");
        var peer = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-Dai.test.conversationSocket=true", "-Dai.test.conversationOnly=true", "-Dai.test.peerFile=" + ready,
                "-cp", System.getProperty("java.class.path"), Regression.class.getName()).redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
        try {
            long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(40);
            while (Files.size(ready) == 0 && peer.isAlive() && System.nanoTime() < end) Thread.sleep(100);
            int other = Integer.parseInt(Files.readString(ready));
            int before = modelRequests.size();
            release.set(new CountDownLatch(1));
            Accepted run = start(base, "origin-node-tab", "routed editor");
            long cursor;
            try (var owner = Tab.open(origin, client, "writer"); var misplaced = Tab.open(other, client, "writer")) {
                cursor = bindAndRead(run, owner, 0).path("cursor").asLong();
                post(run.execution + "/subscriptions", Map.of("subscription", misplaced.challenge, "editorId", run.editor, "after", cursor), false);
                var wrongRoute = URI.create(run.execution.replace(":" + origin + "/", ":" + other + "/") + "/events?after=0");
                var response = HttpClient.newHttpClient().send(HttpRequest.newBuilder(wrongRoute).header("X-DE-Error-Contract", "2")
                        .header("X-Test-Identity", "writer").GET().build(), HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 502 || !response.body().contains("i18n_ai_execution_route_unavailable")
                        || response.body().contains("星河") || misplaced.messages.peek() != null)
                    throw new AssertionError("a different JVM must report a temporary route failure without disclosing execution state");
            }
            var otherSession = URI.create(run.session.replace(":" + origin + "/", ":" + other + "/"));
            var closeOnPeer = HttpClient.newHttpClient().send(HttpRequest.newBuilder(otherSession)
                    .header("X-DE-Error-Contract", "2").header("X-Test-Identity", "writer").DELETE().build(), HttpResponse.BodyHandlers.ofString());
            if (closeOnPeer.statusCode() != 502 || !closeOnPeer.body().contains("i18n_ai_execution_route_unavailable"))
                throw new AssertionError("closing on a peer must report that the origin session is still open");
            release.get().countDown();
            try (var restored = Tab.open(origin, client, "writer")) {
                var page = bindAndRead(run, restored, cursor);
                if (!page.toString().contains("继续") || page.path("events").get(0).path("sequence").asLong() != cursor + 1)
                    throw new AssertionError("restoring the origin route resumes the same cursor");
            }
            close(run); close(run);
            if (modelRequests.size() - before != 1) throw new AssertionError("cross-node repair regenerated an answer");
            String peerBase = base.replace(":" + origin + "/", ":" + other + "/");
            Accepted community = start(peerBase, "community-owner", "community scope");
            try (var writer = Tab.open(other, client, "writer")) {
                if (!bindAndRead(community, writer, 0).toString().contains("星河")) throw new AssertionError("community owner reads own answer");
                for (String token : List.of("other", "writer")) {
                    var request = HttpRequest.newBuilder(URI.create(community.execution + "/events?after=0")).header("X-Test-Identity", token);
                    if ("writer".equals(token)) request.header("X-Test-Scope", "proxy");
                    var deny = HttpClient.newHttpClient().send(request.GET().build(), HttpResponse.BodyHandlers.ofString());
                    if (deny.statusCode() != 403 || deny.body().contains("星河")) throw new AssertionError("community ownership/unsupported proxy");
                }
            }
            System.out.println("PASS independent community-mode node, owner delivery, different user and unsupported proxy refused, one accepted model request");
            System.out.println("PASS two independent JVM HTTP/STOMP nodes, origin delivery, wrong route rejected and cursor replay, one accepted model request");
        } finally {
            peer.getOutputStream().close();
            try {
                if (!peer.waitFor(8, TimeUnit.SECONDS)) peer.destroyForcibly().waitFor();
            }
            finally { Files.deleteIfExists(ready); }
        }
        if (peer.exitValue() != 0) throw new AssertionError("independent node failed its model-count check");
    }

    private static void changedScope(String base, int port, WebSocketStompClient client, List<JsonNode> modelRequests,
                                     java.util.concurrent.atomic.AtomicReference<CountDownLatch> release) throws Exception {
        int before = modelRequests.size();
        for (String context : List.of("workspace", "proxy")) {
            release.set(new CountDownLatch(1));
            Accepted run = start(base, "changed-" + context, "context switch");
            try (var tab = Tab.open(port, client, "writer")) {
                long cursor = bindAndRead(run, tab, 0).path("cursor").asLong();
                var request = HttpRequest.newBuilder(URI.create(run.execution + "/events?after=" + cursor))
                        .header("X-Test-Identity", "writer").header("X-Test-Scope", context).GET().build();
                var denied = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                if (denied.statusCode() != 403 || denied.body().contains("星河")) throw new AssertionError("changed " + context + " cannot read original execution");
                release.get().countDown();
                var notice = tab.messages.poll(5, TimeUnit.SECONDS);
                if (notice == null || !notice.path("denied").asBoolean() || notice.has("events"))
                    throw new AssertionError("changed " + context + " retires original socket");
            }
        }
        if (modelRequests.size() - before != 2) throw new AssertionError("workspace/proxy switch caused a new generation");
        System.out.println("PASS actual HTTP/STOMP rejects changed workspace and proxy identities, two questions / two model requests");
    }

    private static void browserSuite(int port, String spec) throws Exception {
        var browser = new ProcessBuilder("node", "node_modules/@playwright/test/cli.js", "test", "-c",
                "playwright.dataset-calc-assistant.config.cjs", spec);
        browser.directory(new java.io.File("tests/browser"));
        browser.environment().put("DATASET_ASSISTANT_BACKEND", "http://127.0.0.1:" + port);
        var process = browser.inheritIO().start();
        if (!process.waitFor(200, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new AssertionError("editor timeout: " + spec);
        }
        if (process.exitValue() != 0) throw new AssertionError("editor browser exited " + process.exitValue() + ": " + spec);
    }

    static org.springframework.boot.web.server.WebServer editorServer(AiConversationManage manage) throws Exception {
        var factory = new TomcatServletWebServerFactory(0);
        factory.setAddress(InetAddress.getByName("127.0.0.1"));
        factory.addContextCustomizers(ctx -> ctx.addServletContainerInitializer(new WsSci(), Set.of()));
        return factory.getWebServer(servlet -> {
            var context = new GenericWebApplicationContext(servlet);
            context.getBeanFactory().registerSingleton("conversation", manage);
            context.getBeanFactory().registerSingleton("socketIdentity", (WebSocketAuthService) ConversationSocketRegression::actor);
            new AnnotatedBeanDefinitionReader(context).register(WebConfiguration.class, CorsConfig.class, io.dataease.exception.GlobalExceptionHandler.class, WsConfig.class, AiConversationServer.class, io.dataease.ai.server.AiConversationSocket.class);
            var mvc = servlet.addServlet("editor", new DispatcherServlet(context));
            mvc.setLoadOnStartup(1); mvc.addMapping("/");
            var filter = servlet.addFilter("owned-identities", (Filter) (req, res, chain) -> {
                String token = ((HttpServletRequest) req).getHeader("X-Test-Identity");
                Long uid = actor(token);
                if (uid == null) { ((HttpServletResponse) res).sendError(401); return; }
                V3UserUtil.setUid(uid);
                String scope = ((HttpServletRequest) req).getHeader("X-Test-Scope");
                if ("proxy".equals(scope)) V3UserUtil.setProxy(9001L);
                if ("workspace".equals(scope)) workspace.set(9002L);
                try { chain.doFilter(req, res); } finally { V3UserUtil.clear(); workspace.remove(); }
            });
            filter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/de2api/*");
        });
    }

    static void run(AiConversationManage manage, AtomicBoolean resourceAllowed, List<JsonNode> modelRequests, java.util.concurrent.atomic.AtomicReference<CountDownLatch> release, java.util.function.Consumer<Boolean> toolMode, java.util.function.IntConsumer timeout) throws Exception {
        var service = editorServer(manage);
        var client = new WebSocketStompClient(new StandardWebSocketClient());
        client.setMessageConverter(new MappingJackson2MessageConverter());
        try {
            service.start();
            if ("1".equals(System.getenv("AI_EDITOR_LANGUAGE_BROWSER"))) {
                browserSuite(service.getPort(), "dataset-calc-assistant-language.spec.cjs");
                if (modelRequests.size() != 20) throw new AssertionError("unexpected language model requests: " + modelRequests.size());
                System.out.println("PASS 6 Chrome editor/locale combinations, 20 actual conversation HTTP/model requests, scoped directives and new-conversation reset");
                return;
            }
            String ready = System.getProperty("ai.test.peerFile");
            if (ready != null) {
                Files.writeString(Path.of(ready), Integer.toString(service.getPort()));
                System.in.read();
                if (modelRequests.size() != 1) throw new AssertionError("peer serves only its own explicit community request");
                return;
            }
            String base = "http://127.0.0.1:" + service.getPort() + "/de2api/ai/conversations";
            try (var original = Tab.open(service.getPort(), client, "writer");
                 var sibling = Tab.open(service.getPort(), client, "writer");
                 var other = Tab.open(service.getPort(), client, "other")) {
                var session = post(base, Map.of("scene","sql","resourceId","11","editorId","first-tab"), true);
                String sessionUrl = base + "/" + session.path("sessionId").asText();
                var accepted = post(sessionUrl + "/executions", Map.of("contextVersion",session.path("contextVersion").asText(),"text","stream slowly"), true);
                String execution = sessionUrl + "/executions/" + accepted.path("executionId").asText();
                post(execution + "/subscriptions", Map.of("subscription",other.challenge,"editorId","first-tab","after",0), false);
                post(execution + "/subscriptions", Map.of("subscription",sibling.challenge,"editorId","other-tab","after",0), false);
                post(execution + "/subscriptions", Map.of("subscription",original.challenge,"editorId","first-tab","after",0), true);
                CompletableFuture.delayedExecutor(10, TimeUnit.SECONDS).execute(() -> release.get().countDown());
                long sequence = 0; int batches = 0; StringBuilder text = new StringBuilder();
                while (true) {
                    var page = original.messages.poll(6, TimeUnit.SECONDS);
                    if (page == null || !page.path("executionId").equals(accepted.path("executionId")))
                        throw new AssertionError("original tab receives its accepted execution");
                    if (!page.path("events").isEmpty()) batches++;
                    for (var event : page.path("events")) {
                        if (event.path("sequence").asLong() != ++sequence) throw new AssertionError("missing or repeated event");
                        text.append(event.path("text").asText(""));
                    }
                    if (page.path("status").asText().equals("COMPLETED")) break;
                }
                if (batches < 2 || !text.toString().equals("星河继续") || modelRequests.size() != 1
                        || sibling.messages.peek() != null || other.messages.peek() != null)
                    throw new AssertionError("one accepted model call incrementally reaches exactly its original tab");
                try (var revoked = Tab.open(service.getPort(), client, "writer")) {
                    release.set(new CountDownLatch(1));
                    var second = post(sessionUrl + "/executions", Map.of("contextVersion",session.path("contextVersion").asText(),"text","revoke"), true);
                    String secondUrl = sessionUrl + "/executions/" + second.path("executionId").asText();
                    post(secondUrl + "/subscriptions", Map.of("subscription",revoked.challenge,"editorId","first-tab","after",0), true);
                    while (true) {
                        var page = revoked.messages.poll(4, TimeUnit.SECONDS);
                        if (page == null) throw new AssertionError("expected first permitted batch");
                        if (!page.path("events").isEmpty()) break;
                    }
                    resourceAllowed.set(false);
                    var denied = revoked.messages.poll(5, TimeUnit.SECONDS);
                    if (denied == null || !denied.path("denied").asBoolean() || denied.has("events"))
                        throw new AssertionError("revocation stops protected content at the next delivery");
                }
            }
            // Reuse the controlled tool protocol and public run lifecycle, including its failure terminal.
            resourceAllowed.set(true);
            release.get().countDown();
            toolMode.accept(true);
            try (var toolTab = Tab.open(service.getPort(), client, "writer")) {
                var session = post(base, Map.of("scene", "dataset-sql", "resourceId", "11", "editorId", "tool-tab"), true);
                String path = base + "/" + session.path("sessionId").asText() + "/executions";
                var execution = post(path, Map.of("contextVersion", session.path("contextVersion").asText(), "text", "use tool"), true);
                post(path + "/" + execution.path("executionId").asText() + "/subscriptions",
                        Map.of("subscription", toolTab.challenge, "editorId", "tool-tab", "after", 0), true);
                var statuses = new ArrayList<String>();
                long sequence = 0;
                while (true) {
                    var page = toolTab.messages.poll(6, TimeUnit.SECONDS);
                    if (page == null) throw new AssertionError("tool batch missing");
                    for (var event : page.path("events")) {
                        if (event.path("sequence").asLong() != ++sequence) throw new AssertionError("noncontiguous tool events");
                        if (event.hasNonNull("tool") && event.at("/tool/stepNumber").asInt() != 1)
                            throw new AssertionError("The same tool must retain step 1 on each delivery");
                        statuses.add(event.hasNonNull("tool") ? event.path("tool").path("status").asText() : event.path("type").asText());
                    }
                    if (!"RUNNING".equals(page.path("status").asText())) break;
                }
                if (!statuses.equals(List.of("STARTED", "RUNNING", "FAILED", "FAILED")))
                    throw new AssertionError("each state of the real tool and the final execution appear once: " + statuses);
                toolMode.accept(false);
                String sessionUrl = base + "/" + session.path("sessionId").asText();
                String version = session.path("contextVersion").asText();
                var followup = post(path, Map.of("contextVersion", version, "text", "第 1 步完成了吗"), true);
                var current = new Accepted(sessionUrl, path + "/" + followup.path("executionId").asText(), "tool-tab", version);
                for (int i = 0; i < 200 && "RUNNING".equals(read(current, 0).path("status").asText()); i++) Thread.sleep(30);
                var messages = modelRequests.getLast().path("messages");
                var step = json.readTree(messages.get(messages.size() - 1).path("content").asText()).path("historicalStep");
                if (!"COMPLETED".equals(read(current, 0).path("status").asText()) || step.path("stepNumber").asInt() != 1
                        || !"FAILED".equals(step.path("status").asText()) || messages.toString().contains("tool_call_id"))
                    throw new AssertionError("Public HTTP follow-up must locate the failed step, without replaying its protocol");
                close(current);
                System.out.println("PASS stable numbered tool states over STOMP and a status-only follow-up over HTTP");
            } finally { toolMode.accept(false); }
            grace(base, service.getPort(), client, modelRequests, release);
            changedScope(base, service.getPort(), client, modelRequests, release);
            crossNode(base, service.getPort(), client, modelRequests, release);
            timeout.accept(5);
            release.set(new CountDownLatch(1));
            int beforeTimeout = modelRequests.size();
            Accepted limited = start(base, "short-deadline", "hold beyond grace");
            try (var tab = Tab.open(service.getPort(), client, "writer")) { bindAndRead(limited, tab, 0); }
            Thread.sleep(5500);
            var timedOut = read(limited, 0);
            if (!"FAILED".equals(timedOut.path("status").asText()) || !timedOut.toString().contains("timeout")
                    || modelRequests.size() - beforeTimeout != 1) throw new AssertionError("original deadline wins during disconnect");
            close(limited); timeout.accept(60);
            System.out.println("PASS original 5s model deadline during the 30s grace, one model request");
            if ("1".equals(System.getenv("AI_SOCKET_BROWSER"))) {
                resourceAllowed.set(true);
                release.get().countDown();
                release.set(new CountDownLatch(1));
                int before = modelRequests.size();
                browserSuite(service.getPort(), "dataset-calc-assistant-transport.spec.cjs");
                int calls = modelRequests.size() - before;
                if (calls != 10)
                    throw new AssertionError("nine editor scenarios must make nine initial model requests and one explicit retry; actual: " + calls);
                System.out.println("PASS both real editors, 9 scenarios: ten-second streams, zero connected progress GETs, gap repair, reconnect, offline completion, explicit retry, stop, new conversation and drawer close; 9 starts + 1 manual retry = 10 model requests");
            }
            System.out.println("PASS authenticated HTTP/STOMP, ten-second controlled generation, private tabs, foreign user and resource revocation");
        } finally { release.get().countDown(); resourceAllowed.set(true); client.stop(); service.stop(); service.destroy(); manage.destroy(); }
    }
}
