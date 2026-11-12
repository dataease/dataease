package io.dataease.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpServer;
import io.dataease.ai.dao.entity.*;
import io.dataease.i18n.Translator;
import io.dataease.listener.sql.*;
import org.h2.tools.Server;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.*;
import org.springframework.transaction.support.TransactionTemplate;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/** Observable public task behavior across independent JVMs sharing an owned H2 TCP database. */
public final class TaskClusterRegression {
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final Map<String, CountDownLatch> release = new ConcurrentHashMap<>();
    private static final Map<String, Integer> sends = new ConcurrentHashMap<>();
    private static final List<Process> processes = new ArrayList<>();
    private static Path output;
    private static String url;
    private static int checks;
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); checks++; }
    private static HttpResponse<String> call(int port, String path, String method, String body, String actor) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).timeout(java.time.Duration.ofSeconds(10))
                .header("Content-Type", "application/json").header("X-Test-Actor", actor).method(method, HttpRequest.BodyPublishers.ofString(body));
        return HTTP.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
    private static JsonNode call(int port, String path, String method, String body) throws Exception {
        var response = call(port, path, method, body, "1");
        if (response.statusCode() != 200) throw new AssertionError(method + " " + path + " returned " + response.statusCode() + ": " + response.body());
        return TaskNode.JSON.readTree(response.body());
    }
    private static String body(String key) throws Exception {
        return TaskNode.JSON.writeValueAsString(Map.of("submissionKey", key, "capability", "sql.assist", "scene", "sql", "resourceId", "11", "editorId", "cluster", "sql", "select 1", "text", key));
    }
    private static JsonNode await(int port, String path, String state) throws Exception {
        JsonNode result = null;
        for (int i = 0; i < 900; i++) {
            result = call(port, path, "GET", "");
            if (state.equals(result.path("status").asText())) return result;
            if (Set.of("COMPLETED", "FAILED", "INTERRUPTED", "CANCELLED").contains(result.path("status").asText())) throw new AssertionError("expected " + state + ": " + result);
            Thread.sleep(25);
        }
        throw new AssertionError("timed out awaiting " + state + ": " + result);
    }
    private static Process node(String name, boolean schedule) throws Exception {
        var process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(), "-cp", System.getProperty("java.class.path"),
                "io.dataease.ai.TaskNode", url, output.toString(), name, Boolean.toString(schedule))
                .redirectErrorStream(true).redirectOutput(output.resolve(name + ".log").toFile()).start();
        processes.add(process);
        long deadline = System.currentTimeMillis() + 20000;
        while (!Files.exists(output.resolve(name + ".port"))) {
            if (!process.isAlive() || System.currentTimeMillis() > deadline) throw new AssertionError("backend node failed: " + Files.readString(output.resolve(name + ".log")));
            Thread.sleep(25);
        }
        return process;
    }
    private static int port(String name) throws Exception { return Integer.parseInt(Files.readString(output.resolve(name + ".port"))); }
    private static void signal(Process process, String signal) throws Exception {
        check(new ProcessBuilder("/bin/kill", "-" + signal, Long.toString(process.pid())).start().waitFor() == 0, "process signal " + signal);
    }
    public static void run() throws Exception {
        output = Files.createTempDirectory("ai-task-cluster-");
        String oldBind = System.getProperty("h2.bindAddress"); System.setProperty("h2.bindAddress", "127.0.0.1");
        var database = Server.createTcpServer("-tcpPort", "0", "-tcpDaemon", "-ifNotExists").start();
        var provider = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        var workers = Executors.newCachedThreadPool(r -> { var thread = new Thread(r); thread.setDaemon(true); return thread; }); provider.setExecutor(workers);
        provider.createContext("/v1/chat/completions", exchange -> {
            String key = "";
            try {
                var request = TaskNode.JSON.readTree(exchange.getRequestBody()); key = request.path("messages").get(1).path("content").asText();
                sends.merge(key, 1, Integer::sum);
                if (key.startsWith("slow-")) release.computeIfAbsent(key, ignored -> new CountDownLatch(1)).await(40, TimeUnit.SECONDS);
                String content = "data: {\"choices\":[{\"index\":0,\"delta\":{\"content\":\"persisted " + key + "\"},\"finish_reason\":\"stop\"}]}\n\ndata: [DONE]\n\n";
                byte[] body = content.getBytes(StandardCharsets.UTF_8); exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
                exchange.sendResponseHeaders(200, body.length); exchange.getResponseBody().write(body);
            } catch (Exception ignored) { }
            finally { exchange.close(); }
        }); provider.start();
        url = "jdbc:h2:tcp://127.0.0.1:" + database.getPort() + "/mem:ai_tasks;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000";
        try (var factory = TaskNode.factory(url)) {
            var em = SharedEntityManagerCreator.createSharedEntityManager(factory); var manager = new JpaTransactionManager(factory);
            var jdbc = new JdbcTemplate(new DriverManagerDataSource(url, "sa", ""));
            new AiConfigurationInit(jdbc, factory, em, manager).execute(); var migration = new AiTaskInit(jdbc, factory, em, manager); migration.execute(); migration.execute();
            var delivery = new AiTaskDeliveryInit(jdbc, factory); delivery.execute(); delivery.execute();
            new TransactionTemplate(manager).executeWithoutResult(status -> {
                var model = new CoreAiModel(); model.setId(42L); model.setName("controlled task model"); model.setNameKey("controlled-task-model");
                model.setModelIdentifier("cluster-model-v1"); model.setBaseUrl("http://127.0.0.1:" + provider.getAddress().getPort() + "/v1");
                model.setKeyEnvelope(TaskNode.secrets().encrypt("synthetic-key", 42L)); model.setValidatedRevision(1L); em.persist(model);
                var setting = em.find(CoreAiSetting.class, 1L); setting.setEnabled(true); setting.setDefaultModelId(42L); setting.setRequestTimeoutSeconds(60); em.flush();
            });
            // A queued request survives the accepting process without ever having an execution owner.
            var accepting = node("accepting", false); int acceptingPort = port("accepting");
            var queued = call(acceptingPort, "/ai/tasks", "POST", body("queued-after-restart"));
            check("QUEUED".equals(queued.path("status").asText()), "unstarted task durably queued"); accepting.destroyForcibly().waitFor();
            var first = node("first", true); var second = node("second", true); int a = port("first"), b = port("second");
            String queuedPath = "/ai/tasks/" + queued.path("taskId").asText();
            check(await(b, queuedPath, "COMPLETED").path("result").path("text").asText().equals("persisted queued-after-restart"), "queued task scheduled after accepting node restart");
            // Simultaneous retry of one submission at different real HTTP backends yields one task and one model send.
            var pool = Executors.newFixedThreadPool(2);
            try {
                var x = pool.submit(() -> call(a, "/ai/tasks", "POST", body("same-two-nodes")));
                var y = pool.submit(() -> call(b, "/ai/tasks", "POST", body("same-two-nodes")));
                var one = x.get(); var two = y.get(); check(one.path("taskId").equals(two.path("taskId")), "cross-node duplicate submission maps one task");
                await(a, "/ai/tasks/" + one.path("taskId").asText(), "COMPLETED");
                check(sends.getOrDefault("same-two-nodes", 0) == 1, "two independent schedulers send the model exactly once");
            } finally { pool.shutdownNow(); }
            check(call(b, queuedPath, "GET", "", "2").statusCode() == 403, "another trusted actor cannot read task/result");
            Files.createFile(output.resolve("revoked-1"));
            check(call(a, queuedPath, "GET", "", "1").statusCode() == 403, "revoked owner cannot retrieve persisted result"); Files.delete(output.resolve("revoked-1"));
            var cancel = call(a, "/ai/tasks", "POST", body("slow-cancel")); String cancelPath = "/ai/tasks/" + cancel.path("taskId").asText();
            await(a, cancelPath, "RUNNING");
            while (!sends.containsKey("slow-cancel")) Thread.sleep(25);
            check("CANCELLED".equals(call(b, cancelPath + "/cancel", "POST", "").path("status").asText()), "other node durably cancels current attempt");
            release.computeIfAbsent("slow-cancel", ignored -> new CountDownLatch(1)).countDown(); Thread.sleep(500);
            check(call(a, cancelPath, "GET", "").path("result").isNull(), "cancel accepted before model return prevents late result commit");
            var retryPool = Executors.newFixedThreadPool(2);
            try {
                String retryBody = TaskNode.JSON.writeValueAsString(Map.of("retryKey", "same-cross-node-retry", "expectedAttemptId", cancel.path("attemptId").asText()));
                var retryA = retryPool.submit(() -> call(a, cancelPath + "/retry", "POST", retryBody));
                var retryB = retryPool.submit(() -> call(b, cancelPath + "/retry", "POST", retryBody));
                var retriedA = retryA.get(); var retriedB = retryB.get();
                check(retriedA.path("attemptId").equals(retriedB.path("attemptId")) && retriedA.path("attemptNumber").asInt() == 2, "simultaneous cross-node retry creates exactly one new attempt");
                await(b, cancelPath, "COMPLETED");
                check(sends.getOrDefault("slow-cancel", 0) == 2, "manual retry adds exactly one model execution after cancelled original");
            } finally { retryPool.shutdownNow(); }
            // No foreground event polls, page/session state or login activity for longer than the old 15-second lease.
            var detached = call(a, "/ai/tasks", "POST", body("slow-detached")); String detachedPath = "/ai/tasks/" + detached.path("taskId").asText();
            while (!sends.containsKey("slow-detached")) Thread.sleep(25);
            Thread.sleep(16000); release.get("slow-detached").countDown();
            check(await(b, detachedPath, "COMPLETED").path("result").path("text").asText().equals("persisted slow-detached"), "detached task survives old frontend lease without polling");
            var stale = call(a, "/ai/tasks", "POST", body("slow-stale")); String stalePath = "/ai/tasks/" + stale.path("taskId").asText();
            while (!sends.containsKey("slow-stale")) Thread.sleep(25);
            signal(first, "STOP"); signal(second, "STOP");
            var recovered = node("recovered", true); int c = port("recovered");
            check(await(c, stalePath, "INTERRUPTED").path("result").isNull(), "stale node running attempt interrupted without resuming");
            release.get("slow-stale").countDown(); signal(first, "CONT"); signal(second, "CONT"); Thread.sleep(1000);
            check("INTERRUPTED".equals(call(c, stalePath, "GET", "").path("status").asText()) && call(c, stalePath, "GET", "").path("result").isNull(), "resumed old JVM cannot commit after recovery fencing");
            check(sends.getOrDefault("slow-stale", 0) == 1, "node recovery never automatically reexecutes interrupted attempt");
            first.destroyForcibly().waitFor(); second.destroyForcibly().waitFor(); recovered.destroyForcibly().waitFor();
            var restarted = node("restarted", true); int d = port("restarted");
            check(call(d, detachedPath, "GET", "").path("result").path("text").asText().equals("persisted slow-detached"), "completed result and model snapshot survive all backend restarts");
            var pending = call(d, "/ai/tasks/notices", "GET", "");
            check(java.util.stream.StreamSupport.stream(pending.spliterator(), false).anyMatch(notice -> notice.path("attemptId").equals(detached.path("attemptId"))), "offline completion notice survives all backend restarts");
            check(java.util.stream.StreamSupport.stream(pending.spliterator(), false).anyMatch(notice -> "INTERRUPTED".equals(notice.path("status").asText())), "node loss terminal creates durable interrupted completion notice");
            check(call(d, "/ai/tasks/notices", "GET", "", "2").body().equals("[]"), "another actor cannot receive owner's pending completion notices");
            Files.createFile(output.resolve("revoked-1"));
            check(call(d, "/ai/tasks/notices", "GET", "", "1").statusCode() == 403, "revoked actor cannot poll pending notices");
            Files.delete(output.resolve("revoked-1"));
            for (var notice : pending) call(d, "/ai/tasks/notices/" + notice.path("attemptId").asText() + "/ack", "POST", "");
            check(call(d, "/ai/tasks/notices", "GET", "").size() == 0, "acknowledged attempts do not reappear in pending delivery");
            var observer = node("observer", false); int e = port("observer");
            check(call(e, "/ai/tasks/notices", "GET", "").size() == 0, "notice acknowledgement is shared across independent JVM nodes");
            check(call(e, detachedPath, "GET", "").path("result").path("text").asText().equals("persisted slow-detached"), "notice acknowledgement never consumes own task result");
            check(call(d, "/ai/tasks", "GET", "").size() == 0, "assistant active query does not list terminal task history");
            System.out.println("PASS task cluster " + checks + " public HTTP assertions (independent JVM nodes, shared isolated H2 TCP, controlled identity/scene/model; no Redis/global model-limit or XPack claim)");
        } finally {
            for (var process : processes) if (process.isAlive()) { try { signal(process, "CONT"); } catch (Exception ignored) { } process.destroyForcibly().waitFor(); }
            release.values().forEach(CountDownLatch::countDown); provider.stop(0); workers.shutdownNow(); database.stop();
            if (oldBind == null) System.clearProperty("h2.bindAddress"); else System.setProperty("h2.bindAddress", oldBind);
            try (var files = Files.walk(output)) { for (Path path : files.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path); }
        }
    }
}
