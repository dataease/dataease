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
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** Real loopback editor HTTP, platform STOMP and the independently controlled upstream model. */
public final class ConversationSocketRegression {
    @Configuration
    @EnableWebMvc
    @EnableWebSocketMessageBroker
    static class WebConfiguration { }
    static final ObjectMapper json = new ObjectMapper();

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
            var socket = client.connectAsync("ws://127.0.0.1:" + port + "/websocket/websocket?token=" + actor,
                    new StompSessionHandlerAdapter() {}).get(8, TimeUnit.SECONDS);
            var messages = new LinkedBlockingQueue<JsonNode>();
            socket.subscribe("/app/ai/conversation", new StompFrameHandler() {
                public Type getPayloadType(StompHeaders headers) { return JsonNode.class; }
                public void handleFrame(StompHeaders headers, Object body) {
                    messages.add((JsonNode) body);
                }
            });
            var ready = messages.poll(5, TimeUnit.SECONDS);
            if (ready == null || !ready.hasNonNull("subscription")) throw new AssertionError("the originating tab must receive its private binding challenge");
            return new Tab(socket, messages, ready.path("subscription").asText());
        }
        public void close() { if (socket.isConnected()) socket.disconnect(); }
    }

    static void run(AiConversationManage manage, AtomicBoolean resourceAllowed, List<JsonNode> modelRequests, java.util.concurrent.atomic.AtomicReference<CountDownLatch> release, java.util.function.Consumer<Boolean> toolMode) throws Exception {
        var factory = new TomcatServletWebServerFactory(0);
        factory.setAddress(InetAddress.getByName("127.0.0.1"));
        factory.addContextCustomizers(ctx -> ctx.addServletContainerInitializer(new WsSci(), Set.of()));
        var service = factory.getWebServer(servlet -> {
            var context = new GenericWebApplicationContext(servlet);
            context.getBeanFactory().registerSingleton("conversation", manage);
            context.getBeanFactory().registerSingleton("socketIdentity", (WebSocketAuthService) ConversationSocketRegression::actor);
            new AnnotatedBeanDefinitionReader(context).register(WebConfiguration.class, CorsConfig.class, WsConfig.class, AiConversationServer.class, io.dataease.ai.server.AiConversationSocket.class);
            var mvc = servlet.addServlet("editor", new DispatcherServlet(context));
            mvc.setLoadOnStartup(1); mvc.addMapping("/");
            var filter = servlet.addFilter("owned-identities", (Filter) (req, res, chain) -> {
                String token = ((HttpServletRequest) req).getHeader("X-Test-Identity");
                Long uid = actor(token);
                if (uid == null) { ((HttpServletResponse) res).sendError(401); return; }
                V3UserUtil.setUid(uid);
                try { chain.doFilter(req, res); } finally { V3UserUtil.clear(); }
            });
            filter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/de2api/*");
        });
        var client = new WebSocketStompClient(new StandardWebSocketClient());
        client.setMessageConverter(new MappingJackson2MessageConverter());
        try {
            service.start();
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
                var session = post(base, Map.of("scene", "sql", "resourceId", "11", "editorId", "tool-tab"), true);
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
                        statuses.add(event.hasNonNull("tool") ? event.path("tool").path("status").asText() : event.path("type").asText());
                    }
                    if (!"RUNNING".equals(page.path("status").asText())) break;
                }
                if (!statuses.equals(List.of("STARTED", "RUNNING", "FAILED", "FAILED")))
                    throw new AssertionError("each state of the real tool and the final execution appear once: " + statuses);
                System.out.println("PASS same execution delivered every tool state and terminal exactly once");
            } finally { toolMode.accept(false); }
            if ("1".equals(System.getenv("AI_SOCKET_BROWSER"))) {
                resourceAllowed.set(true);
                release.get().countDown();
                release.set(new CountDownLatch(1));
                int before = modelRequests.size();
                var browser = new ProcessBuilder("node", "node_modules/@playwright/test/cli.js", "test", "-c",
                        "playwright.dataset-calc-assistant.config.cjs", "dataset-calc-assistant-transport.spec.cjs");
                browser.directory(new java.io.File("tests/browser"));
                browser.environment().put("DATASET_ASSISTANT_BACKEND", "http://127.0.0.1:" + service.getPort());
                var process = browser.inheritIO().start();
                if (!process.waitFor(100, TimeUnit.SECONDS)) { process.destroyForcibly(); throw new AssertionError("editor timeout"); }
                if (process.exitValue() != 0 || modelRequests.size() - before != 3)
                    throw new AssertionError("each editor scenario should accept one model request, including after gap recovery");
                System.out.println("PASS both real editors, ten-second model streams, zero progress GETs while connected, duplicate/out-of-order delivery and single cursor repair");
            }
            System.out.println("PASS authenticated HTTP/STOMP, ten-second controlled generation, private tabs, foreign user and resource revocation");
        } finally { release.get().countDown(); resourceAllowed.set(true); client.stop(); service.stop(); service.destroy(); manage.destroy(); }
    }
}
