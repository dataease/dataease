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
        return call(port, path, method, body, actor, null);
    }
    private static HttpResponse<String> call(int port, String path, String method, String body, String actor, String proxy) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).timeout(java.time.Duration.ofSeconds(10))
                .header("Content-Type", "application/json").header("X-Test-Actor", actor).method(method, HttpRequest.BodyPublishers.ofString(body));
        if (proxy != null) request.header("X-Test-Proxy", proxy);
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
    private static String settingsBody(JsonNode current, int running, int queued, int perUser, int wait) throws Exception {
        var value = (com.fasterxml.jackson.databind.node.ObjectNode) current.deepCopy();
        value.put("expectedVersion", current.path("rowVersion").asLong());
        var limits = value.putObject("taskScheduling");
        limits.put("maxRunning", running); limits.put("maxQueued", queued); limits.put("maxQueuedPerUser", perUser); limits.put("maxWaitSeconds", wait);
        return TaskNode.JSON.writeValueAsString(value);
    }
    private static JsonNode settings(int port, int running, int queued, int perUser, int wait) throws Exception {
        return call(port, "/ai/settings", "POST", settingsBody(call(port, "/ai/settings", "GET", ""), running, queued, perUser, wait));
    }
    private static void awaitSend(int port, String path, String key) throws Exception { awaitSend(port, path, key, "1"); }
    private static void awaitSend(int port, String path, String key, String actor) throws Exception {
        long deadline = System.currentTimeMillis() + 10000;
        while (!sends.containsKey(key) && System.currentTimeMillis() < deadline) {
            var task = TaskNode.JSON.readTree(call(port, path, "GET", "", actor).body());
            if (Set.of("COMPLETED", "FAILED", "INTERRUPTED", "CANCELLED").contains(task.path("status").asText())) throw new AssertionError("expected provider send for " + key + ": " + task);
            Thread.sleep(25);
        }
        check(sends.containsKey(key), "bounded wait observed model send: " + key);
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
    private static void legacyUpgrade(JdbcTemplate jdbc, jakarta.persistence.EntityManagerFactory factory,
                                      jakarta.persistence.EntityManager em, org.springframework.transaction.PlatformTransactionManager manager) {
        // Owned historical-schema fixture. Migration facts may be observed in this isolated metadata database.
        for (String column : List.of("task_max_running", "task_max_queued", "task_max_queued_per_user", "task_max_wait_seconds")) jdbc.execute("alter table core_ai_setting drop column " + column);
        jdbc.execute("alter table core_ai_task drop column actor_id");
        jdbc.execute("alter table core_ai_task_attempt drop column queue_deadline");
        jdbc.execute("alter table core_ai_task_attempt drop column queue_sequence");
        long created = System.currentTimeMillis() - 1000;
        for (long id : List.of(901L, 903L)) {
            jdbc.update("insert into core_ai_task(id,current_attempt_id,owner_binding,submission_hash,input_hash,owner_json,input_json,context_json,created_at) values(?,?,?,?,?,?,?,?,?)",
                    id, id + 1, "old-binding-" + id, "legacy-submission-" + id, "legacy-input-digest", "{\"actor\":1}", "{}", "{}", created);
            jdbc.update("insert into core_ai_task_attempt(id,task_id,attempt_number,retry_hash,status,created_at,result_json,retry_safe) values(?,?,?,?,?,?,?,?)",
                    id + 1, id, 1, "legacy-retry-" + id, id == 901L ? "QUEUED" : "COMPLETED", created, id == 901L ? null : "historical-result", false);
        }
        var upgrade = new AiTaskSchedulingInit(jdbc, factory, em, manager); upgrade.execute();
        check(jdbc.queryForObject("select queue_deadline from core_ai_task_attempt where id=902", Long.class) == created + 300000,
                "old accepted queue gets fixed default wait from original acceptance time");
        check(jdbc.queryForObject("select actor_id from core_ai_task where id=901", Long.class) == 1L,
                "legacy actor backfill uses original server identity without changing submission hash");
        jdbc.update("update core_ai_task_attempt set queue_deadline=? where id=902", created + 123000);
        upgrade.execute();
        check(jdbc.queryForObject("select queue_deadline from core_ai_task_attempt where id=902", Long.class) == created + 123000,
                "retrying partially applied scheduler migration preserves existing wait deadline");
        check(jdbc.queryForObject("select queue_deadline from core_ai_task_attempt where id=904", Long.class) == null
                && "historical-result".equals(jdbc.queryForObject("select result_json from core_ai_task_attempt where id=904", String.class))
                && "legacy-input-digest".equals(jdbc.queryForObject("select input_hash from core_ai_task where id=901", String.class)),
                "terminal history and input/submission compatibility are not rewritten by queue migration");
        jdbc.update("delete from core_ai_task_attempt where id in (902,904)"); jdbc.update("delete from core_ai_task where id in (901,903)");
    }
    public static void main(String[] args) throws Exception { run(); }
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
                if (key.startsWith("retry-slot-") && sends.get(key) == 1) {
                    byte[] temporary = "{\"error\":{\"message\":\"controlled transient provider failure\"}}".getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "application/json"); exchange.sendResponseHeaders(503, temporary.length);
                    exchange.getResponseBody().write(temporary); return;
                }
                if ("execution-after-wait".equals(key)) Thread.sleep(2500);
                if (key.startsWith("slow-")) release.computeIfAbsent(key, ignored -> new CountDownLatch(1)).await(40, TimeUnit.SECONDS);
                String content = "data: {\"choices\":[{\"index\":0,\"delta\":{\"content\":\"persisted " + key + "\"},\"finish_reason\":\"stop\"}]}\n\ndata: [DONE]\n\n";
                if (key.startsWith("read-slot-") && !request.path("messages").toString().contains("tool-result"))
                    content = "data: {\"choices\":[{\"index\":0,\"delta\":{\"tool_calls\":[{\"index\":0,\"id\":\"inspect-1\",\"type\":\"function\",\"function\":{\"name\":\"inspect\",\"arguments\":\"{}\"}}]},\"finish_reason\":\"tool_calls\"}]}\n\ndata: [DONE]\n\n";
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
            legacyUpgrade(jdbc, factory, em, manager);
            new TransactionTemplate(manager).executeWithoutResult(status -> {
                var model = new CoreAiModel(); model.setId(42L); model.setName("controlled task model"); model.setNameKey("controlled-task-model");
                model.setModelIdentifier("cluster-model-v1"); model.setBaseUrl("http://127.0.0.1:" + provider.getAddress().getPort() + "/v1");
                model.setKeyEnvelope(TaskNode.secrets().encrypt("synthetic-key", 42L)); model.setValidatedRevision(1L); em.persist(model);
                var setting = em.find(CoreAiSetting.class, 1L); setting.setEnabled(true); setting.setDefaultModelId(42L); setting.setRequestTimeoutSeconds(60); em.flush();
            });
            // A queued request survives the accepting process without ever having an execution owner.
            var accepting = node("accepting", false); int acceptingPort = port("accepting");
            var originalSettings = call(acceptingPort, "/ai/settings", "GET", "");
            check(originalSettings.path("taskScheduling").path("maxRunning").asInt() == 2
                    && originalSettings.path("taskScheduling").path("maxQueued").asInt() == 100
                    && originalSettings.path("taskScheduling").path("maxQueuedPerUser").asInt() == 10
                    && originalSettings.path("taskScheduling").path("maxWaitSeconds").asInt() == 300,
                    "administrator settings expose separate task scheduling defaults");
            var capacity = new ArrayList<JsonNode>();
            for (int i = 0; i < 10; i++) capacity.add(call(acceptingPort, "/ai/tasks", "POST", body("queue-default-" + i)));
            check(call(acceptingPort, "/ai/tasks", "POST", body("queue-default-overflow"), "1").statusCode() != 200,
                    "default per-actor queue accepts ten and clearly rejects eleventh");
            check(call(acceptingPort, "/ai/tasks", "POST", body("queue-proxy-overflow"), "1", "777").statusCode() == 409,
                    "same real actor cannot bypass personal queue quota with another proxy/workspace binding");
            check(call(acceptingPort, "/ai/tasks", "GET", "").size() == 10, "queue-full rejection leaves no false acceptance");
            check(call(acceptingPort, "/ai/tasks", "POST", body("queue-default-0")).path("taskId").equals(capacity.get(0).path("taskId")),
                    "same submission replay precedes capacity checks");
            for (var row : capacity) call(acceptingPort, "/ai/tasks/" + row.path("taskId").asText() + "/cancel", "POST", "");
            var settingsBaseline = call(acceptingPort, "/ai/settings", "GET", "");
            String staleSettings = settingsBody(settingsBaseline, 2, 2, 2, 300);
            check(call(acceptingPort, "/ai/settings", "POST", settingsBody(settingsBaseline, 2, 2, 2, 300), "2").statusCode() == 403,
                    "ordinary actor without AI configuration permission cannot change shared task capacity");
            check(call(acceptingPort, "/ai/settings", "POST", settingsBody(settingsBaseline, 0, 2, 2, 300), "1").statusCode() != 200,
                    "invalid task capacity is rejected by settings API");
            call(acceptingPort, "/ai/settings", "POST", staleSettings);
            check(call(acceptingPort, "/ai/settings", "POST", staleSettings, "1").statusCode() == 409, "stale administrator settings update cannot overwrite newer capacity");
            var globalOne = call(acceptingPort, "/ai/tasks", "POST", body("global-queue-one"));
            var globalTwo = TaskNode.JSON.readTree(call(acceptingPort, "/ai/tasks", "POST", body("global-queue-two"), "2").body());
            check(call(acceptingPort, "/ai/tasks", "POST", body("global-queue-overflow"), "2").statusCode() != 200,
                    "global queued capacity rejects across actors before false acceptance");
            call(acceptingPort, "/ai/tasks/" + globalOne.path("taskId").asText() + "/cancel", "POST", "");
            call(acceptingPort, "/ai/tasks/" + globalTwo.path("taskId").asText() + "/cancel", "POST", "", "2");
            check(call(acceptingPort, "/ai/tasks", "POST", body("unknown-resource").replace("\"11\"", "\"99\""), "1").statusCode() == 403,
                    "unknown or unauthorized resource is rejected before queue admission");
            var revokedBeforeStart = TaskNode.JSON.readTree(call(acceptingPort, "/ai/tasks", "POST", body("resource-revoked-before-start"), "2").body());
            Files.createFile(output.resolve("resource-revoked-2"));
            var disable = (com.fasterxml.jackson.databind.node.ObjectNode) call(acceptingPort, "/ai/settings", "GET", "");
            disable.put("expectedVersion", disable.path("rowVersion").asLong()); disable.put("enabled", false);
            disable.put("impactToken", call(acceptingPort, "/ai/settings/impact?defaultModelId=42&enabled=false", "GET", "").path("token").asText());
            call(acceptingPort, "/ai/settings", "POST", TaskNode.JSON.writeValueAsString(disable));
            check(call(acceptingPort, "/ai/tasks", "POST", body("disabled-rejected"), "1").statusCode() != 200,
                    "disabled AI is rejected before queue acceptance");
            var enable = (com.fasterxml.jackson.databind.node.ObjectNode) call(acceptingPort, "/ai/settings", "GET", "");
            enable.put("expectedVersion", enable.path("rowVersion").asLong()); enable.put("enabled", true);
            call(acceptingPort, "/ai/settings", "POST", TaskNode.JSON.writeValueAsString(enable));
            settings(acceptingPort, 2, 100, 10, 1);
            var expiring = call(acceptingPort, "/ai/tasks", "POST", body("expired-before-start"));
            settings(acceptingPort, 2, 100, 10, 300);
            var budgetPolicy = (com.fasterxml.jackson.databind.node.ObjectNode) call(acceptingPort, "/ai/settings", "GET", "");
            budgetPolicy.put("expectedVersion", budgetPolicy.path("rowVersion").asLong()); budgetPolicy.put("requestTimeoutSeconds", 5);
            call(acceptingPort, "/ai/settings", "POST", TaskNode.JSON.writeValueAsString(budgetPolicy));
            var budgetTask = call(acceptingPort, "/ai/tasks", "POST", body("execution-after-wait"));
            var startingRead = call(acceptingPort, "/ai/tasks", "POST", body("deadline-includes-initial-read").replace("select 1", "controlled-start-read"));
            Files.createFile(output.resolve("start-read-enabled"));
            Thread.sleep(3100);
            var queued = call(acceptingPort, "/ai/tasks", "POST", body("queued-after-restart"));
            check("QUEUED".equals(queued.path("status").asText()), "unstarted task durably queued"); accepting.destroyForcibly().waitFor();
            var first = node("first", true); var second = node("second", true); int a = port("first"), b = port("second");
            String queuedPath = "/ai/tasks/" + queued.path("taskId").asText();
            check(await(b, queuedPath, "COMPLETED").path("result").path("text").asText().equals("persisted queued-after-restart"), "queued task scheduled after accepting node restart");
            var expired = await(a, "/ai/tasks/" + expiring.path("taskId").asText(), "FAILED");
            check("queue_timeout".equals(expired.path("errorCode").asText()) && !sends.containsKey("expired-before-start"),
                    "accepted queue deadline stays fixed after configuration change and expired work never sends model request");
            Files.delete(output.resolve("resource-revoked-2"));
            var revokedResult = TaskNode.JSON.readTree(call(b, "/ai/tasks/" + revokedBeforeStart.path("taskId").asText(), "GET", "", "2").body());
            check("FAILED".equals(revokedResult.path("status").asText()) && !sends.containsKey("resource-revoked-before-start"),
                    "resource permission is rechecked before claimed queued work can execute");
            var budgetResult = await(a, "/ai/tasks/" + budgetTask.path("taskId").asText(), "COMPLETED");
            check(budgetResult.path("finishedAt").asLong() - budgetResult.path("createdAt").asLong() > 5000
                    && budgetResult.path("finishedAt").asLong() - budgetResult.path("startedAt").asLong() < 5000,
                    "execution has its own fixed five-second budget beginning after queue wait");
            var readDeadline = await(b, "/ai/tasks/" + startingRead.path("taskId").asText(), "FAILED");
            check("timeout".equals(readDeadline.path("errorCode").asText()) && !sends.containsKey("deadline-includes-initial-read"),
                    "initial authorized data read counts toward the execution deadline fixed when RUNNING starts");
            var restorePolicy = (com.fasterxml.jackson.databind.node.ObjectNode) call(a, "/ai/settings", "GET", "");
            restorePolicy.put("expectedVersion", restorePolicy.path("rowVersion").asLong()); restorePolicy.put("requestTimeoutSeconds", 60);
            call(a, "/ai/settings", "POST", TaskNode.JSON.writeValueAsString(restorePolicy));
            // Simultaneous retry of one submission at different real HTTP backends yields one task and one model send.
            var pool = Executors.newFixedThreadPool(2);
            try {
                var x = pool.submit(() -> call(a, "/ai/tasks", "POST", body("same-two-nodes")));
                var y = pool.submit(() -> call(b, "/ai/tasks", "POST", body("same-two-nodes")));
                var one = x.get(); var two = y.get(); check(one.path("taskId").equals(two.path("taskId")), "cross-node duplicate submission maps one task");
                await(a, "/ai/tasks/" + one.path("taskId").asText(), "COMPLETED");
                check(sends.getOrDefault("same-two-nodes", 0) == 1, "two independent schedulers send the model exactly once");
            } finally { pool.shutdownNow(); }
            var capOne = call(a, "/ai/tasks", "POST", body("slow-cap-one"));
            var capTwo = call(b, "/ai/tasks", "POST", body("slow-cap-two"));
            await(a, "/ai/tasks/" + capOne.path("taskId").asText(), "RUNNING");
            await(b, "/ai/tasks/" + capTwo.path("taskId").asText(), "RUNNING");
            var capThree = call(b, "/ai/tasks", "POST", body("slow-cap-three"));
            Thread.sleep(750);
            check("QUEUED".equals(call(a, "/ai/tasks/" + capThree.path("taskId").asText(), "GET", "").path("status").asText())
                    && !sends.containsKey("slow-cap-three"), "two independent JVMs share exactly two Task RUNNING slots");
            var fairA1 = call(a, "/ai/tasks", "POST", body("slow-fair-a1"));
            var fairA2 = call(b, "/ai/tasks", "POST", body("slow-fair-a2"));
            var fairB1 = TaskNode.JSON.readTree(call(b, "/ai/tasks", "POST", body("slow-fair-b1"), "2").body());
            var fairB2 = TaskNode.JSON.readTree(call(a, "/ai/tasks", "POST", body("slow-fair-b2"), "2").body());
            for (var entry : List.of(capOne, capTwo, capThree)) call(a, "/ai/tasks/" + entry.path("taskId").asText() + "/cancel", "POST", "");
            for (var key : List.of("slow-cap-one", "slow-cap-two", "slow-cap-three")) release.computeIfAbsent(key, ignored -> new CountDownLatch(1)).countDown();
            awaitSend(a, "/ai/tasks/" + fairA1.path("taskId").asText(), "slow-fair-a1");
            awaitSend(b, "/ai/tasks/" + fairB1.path("taskId").asText(), "slow-fair-b1", "2");
            check(sends.containsKey("slow-fair-a1") && sends.containsKey("slow-fair-b1") && !sends.containsKey("slow-fair-a2") && !sends.containsKey("slow-fair-b2"),
                    "shared scheduler alternates actors while preserving each actor FIFO");
            call(a, "/ai/tasks/" + fairA1.path("taskId").asText() + "/cancel", "POST", "");
            call(b, "/ai/tasks/" + fairB1.path("taskId").asText() + "/cancel", "POST", "", "2");
            awaitSend(a, "/ai/tasks/" + fairA2.path("taskId").asText(), "slow-fair-a2");
            awaitSend(b, "/ai/tasks/" + fairB2.path("taskId").asText(), "slow-fair-b2", "2");
            check(sends.containsKey("slow-fair-a2") && sends.containsKey("slow-fair-b2"), "next oldest task for each actor starts after prior work releases slots");
            for (var entry : List.of(fairA1, fairA2)) call(a, "/ai/tasks/" + entry.path("taskId").asText() + "/cancel", "POST", "");
            for (var entry : List.of(fairB1, fairB2)) call(b, "/ai/tasks/" + entry.path("taskId").asText() + "/cancel", "POST", "", "2");
            for (var key : List.of("slow-fair-a1", "slow-fair-a2", "slow-fair-b1", "slow-fair-b2")) release.computeIfAbsent(key, ignored -> new CountDownLatch(1)).countDown();
            check(call(b, queuedPath, "GET", "", "2").statusCode() == 403, "another trusted actor cannot read task/result");
            Files.createFile(output.resolve("revoked-1"));
            check(call(a, queuedPath, "GET", "", "1").statusCode() == 403, "revoked owner cannot retrieve persisted result"); Files.delete(output.resolve("revoked-1"));
            var cancel = call(a, "/ai/tasks", "POST", body("slow-cancel")); String cancelPath = "/ai/tasks/" + cancel.path("taskId").asText();
            await(a, cancelPath, "RUNNING");
            awaitSend(a, cancelPath, "slow-cancel");
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
            var readingOne = call(a, "/ai/tasks", "POST", body("read-slot-one").replace("select 1", "controlled-read"));
            var readingTwo = TaskNode.JSON.readTree(call(b, "/ai/tasks", "POST", body("read-slot-two").replace("select 1", "controlled-read"), "2").body());
            long toolDeadline = System.currentTimeMillis() + 10000;
            while ((!Files.exists(output.resolve("tool-started-1")) || !Files.exists(output.resolve("tool-started-2"))) && System.currentTimeMillis() < toolDeadline) Thread.sleep(25);
            check(Files.exists(output.resolve("tool-started-1")) && Files.exists(output.resolve("tool-started-2")), "both background tasks entered controlled external metadata reads");
            var duringRead = call(b, "/ai/tasks", "POST", body("queued-during-read"));
            Thread.sleep(500);
            check("QUEUED".equals(call(a, "/ai/tasks/" + duringRead.path("taskId").asText(), "GET", "").path("status").asText()) && !sends.containsKey("queued-during-read"),
                    "external data read retains complete Task slot after its model HTTP permit is released");
            var frontSession = call(a, "/ai/conversations", "POST", "{\"scene\":\"sql\",\"resourceId\":\"11\",\"editorId\":\"front-independent\",\"sql\":\"select 1\"}");
            String frontPath = "/ai/conversations/" + frontSession.path("sessionId").asText();
            var front = call(a, frontPath + "/executions", "POST", TaskNode.JSON.writeValueAsString(Map.of("contextVersion", frontSession.path("contextVersion").asText(), "text", "foreground-during-tool-wait")));
            String eventPath = frontPath + "/executions/" + front.path("executionId").asText() + "/events";
            check("COMPLETED".equals(await(a, eventPath, "COMPLETED").path("status").asText()), "foreground model request remains available while complete Task slots are occupied by data reads");
            call(a, frontPath, "DELETE", "");
            Files.createFile(output.resolve("release-tools"));
            await(a, "/ai/tasks/" + readingOne.path("taskId").asText(), "COMPLETED");
            long readingDeadline = System.currentTimeMillis() + 10000;
            JsonNode readingResult;
            do { readingResult = TaskNode.JSON.readTree(call(b, "/ai/tasks/" + readingTwo.path("taskId").asText(), "GET", "", "2").body()); if ("COMPLETED".equals(readingResult.path("status").asText())) break; Thread.sleep(25); } while (System.currentTimeMillis() < readingDeadline);
            check("COMPLETED".equals(readingResult.path("status").asText()), "second actor read task completes after controlled data source release");
            await(b, "/ai/tasks/" + duringRead.path("taskId").asText(), "COMPLETED");
            settings(a, 1, 100, 10, 300);
            var retryPolicy = (com.fasterxml.jackson.databind.node.ObjectNode) call(a, "/ai/settings", "GET", "");
            var previousRecovery = retryPolicy.path("recovery").deepCopy();
            retryPolicy.put("expectedVersion", retryPolicy.path("rowVersion").asLong());
            var delayedRecovery = retryPolicy.putObject("recovery"); delayedRecovery.put("stepRetries", 1); delayedRecovery.put("attemptRetries", 1); delayedRecovery.put("initialDelayMillis", 3000);
            call(a, "/ai/settings", "POST", TaskNode.JSON.writeValueAsString(retryPolicy));
            var retryWait = call(a, "/ai/tasks", "POST", body("retry-slot-resume"));
            String retryWaitPath = "/ai/tasks/" + retryWait.path("taskId").asText();
            JsonNode waiting = null; long retryDeadline = System.currentTimeMillis() + 10000;
            do { waiting = call(b, retryWaitPath, "GET", ""); if (waiting.path("recovery").path("technicalRetries").asInt() == 1) break; Thread.sleep(25); } while (System.currentTimeMillis() < retryDeadline);
            check("RUNNING".equals(waiting.path("status").asText()) && waiting.path("recovery").path("technicalRetries").asInt() == 1,
                    "automatic retry backoff remains the same running task");
            var queuedDuringRetry = call(b, "/ai/tasks", "POST", body("queued-during-auto-retry"));
            Thread.sleep(500);
            check("QUEUED".equals(call(a, "/ai/tasks/" + queuedDuringRetry.path("taskId").asText(), "GET", "").path("status").asText())
                    && !sends.containsKey("queued-during-auto-retry") && sends.getOrDefault("retry-slot-resume", 0) == 1,
                    "retry backoff retains its complete task slot while no model HTTP request is in flight");
            var retried = await(b, retryWaitPath, "COMPLETED");
            check(retried.path("attemptId").equals(retryWait.path("attemptId")) && retried.path("startedAt").equals(waiting.path("startedAt"))
                    && sends.getOrDefault("retry-slot-resume", 0) == 2, "controlled technical retry reuses the original task attempt and start time");
            await(a, "/ai/tasks/" + queuedDuringRetry.path("taskId").asText(), "COMPLETED");
            var cancelledWait = call(a, "/ai/tasks", "POST", body("retry-slot-cancel"));
            String cancelledWaitPath = "/ai/tasks/" + cancelledWait.path("taskId").asText();
            retryDeadline = System.currentTimeMillis() + 10000;
            do { waiting = call(b, cancelledWaitPath, "GET", ""); if (waiting.path("recovery").path("technicalRetries").asInt() == 1) break; Thread.sleep(25); } while (System.currentTimeMillis() < retryDeadline);
            check(waiting.path("recovery").path("technicalRetries").asInt() == 1, "second task entered an observable automatic retry wait");
            var queuedAfterCancel = call(b, "/ai/tasks", "POST", body("queued-after-retry-cancel"));
            call(b, cancelledWaitPath + "/cancel", "POST", "");
            await(a, "/ai/tasks/" + queuedAfterCancel.path("taskId").asText(), "COMPLETED");
            Thread.sleep(3300);
            check("CANCELLED".equals(call(a, cancelledWaitPath, "GET", "").path("status").asText()) && sends.getOrDefault("retry-slot-cancel", 0) == 1,
                    "cross-node cancel releases the waiting task slot and prevents its delayed model retry");
            var restoredRecovery = (com.fasterxml.jackson.databind.node.ObjectNode) call(a, "/ai/settings", "GET", "");
            restoredRecovery.put("expectedVersion", restoredRecovery.path("rowVersion").asLong()); restoredRecovery.set("recovery", previousRecovery);
            call(a, "/ai/settings", "POST", TaskNode.JSON.writeValueAsString(restoredRecovery));
            settings(a, 2, 100, 10, 300);
            var lowerOne = call(a, "/ai/tasks", "POST", body("slow-lower-one"));
            var lowerTwo = call(b, "/ai/tasks", "POST", body("slow-lower-two"));
            await(a, "/ai/tasks/" + lowerOne.path("taskId").asText(), "RUNNING");
            await(b, "/ai/tasks/" + lowerTwo.path("taskId").asText(), "RUNNING");
            var lowerOldOne = call(a, "/ai/tasks", "POST", body("slow-lower-old-one"));
            var lowerOldTwo = call(b, "/ai/tasks", "POST", body("slow-lower-old-two"));
            settings(a, 1, 1, 1, 1);
            Thread.sleep(750);
            check("RUNNING".equals(call(b, "/ai/tasks/" + lowerOne.path("taskId").asText(), "GET", "").path("status").asText())
                    && "RUNNING".equals(call(a, "/ai/tasks/" + lowerTwo.path("taskId").asText(), "GET", "").path("status").asText()),
                    "lowering capacity preserves existing running work across nodes");
            release.computeIfAbsent("slow-lower-one", ignored -> new CountDownLatch(1)).countDown();
            check("COMPLETED".equals(await(b, "/ai/tasks/" + lowerOne.path("taskId").asText(), "COMPLETED").path("status").asText()),
                    "capacity-only settings change does not invalidate accepted execution policy");
            check(call(a, "/ai/tasks", "POST", body("lower-overflow"), "1").statusCode() != 200,
                    "lower queue capacity pauses new acceptance without deleting accepted tasks");
            call(a, "/ai/tasks/" + lowerOne.path("taskId").asText() + "/cancel", "POST", "");
            Thread.sleep(1250);
            check("QUEUED".equals(call(b, "/ai/tasks/" + lowerOldOne.path("taskId").asText(), "GET", "").path("status").asText())
                    && "QUEUED".equals(call(a, "/ai/tasks/" + lowerOldTwo.path("taskId").asText(), "GET", "").path("status").asText()),
                    "start waits until running count is below lowered limit and old wait deadlines stay unchanged");
            call(b, "/ai/tasks/" + lowerTwo.path("taskId").asText() + "/cancel", "POST", "");
            await(a, "/ai/tasks/" + lowerOldOne.path("taskId").asText(), "RUNNING");
            check("QUEUED".equals(call(b, "/ai/tasks/" + lowerOldTwo.path("taskId").asText(), "GET", "").path("status").asText()), "lowered shared running limit starts only one accepted task");
            for (var row : List.of(lowerOne, lowerTwo, lowerOldOne, lowerOldTwo)) call(a, "/ai/tasks/" + row.path("taskId").asText() + "/cancel", "POST", "");
            for (var key : List.of("slow-lower-one", "slow-lower-two", "slow-lower-old-one", "slow-lower-old-two")) release.computeIfAbsent(key, ignored -> new CountDownLatch(1)).countDown();
            settings(a, 2, 100, 10, 300);
            // Only node A published the last change. Force B to own the next fresh request.
            signal(first, "STOP");
            try {
                var otherNode = call(b, "/ai/tasks", "POST", body("fresh-capacity-other-node"));
                check("COMPLETED".equals(await(b, "/ai/tasks/" + otherNode.path("taskId").asText(), "COMPLETED").path("status").asText()),
                        "a second standalone node admits current committed policy after capacity update on another node");
            } finally { signal(first, "CONT"); }
            if (Boolean.getBoolean("ai.test.schedulingOnly")) {
                System.out.println("PASS task scheduling " + checks + " public HTTP assertions across independent JVMs");
                return;
            }
            // No foreground event polls, page/session state or login activity for longer than the old 15-second lease.
            var detached = call(a, "/ai/tasks", "POST", body("slow-detached")); String detachedPath = "/ai/tasks/" + detached.path("taskId").asText();
            awaitSend(a, detachedPath, "slow-detached");
            Thread.sleep(16000); release.get("slow-detached").countDown();
            check(await(b, detachedPath, "COMPLETED").path("result").path("text").asText().equals("persisted slow-detached"), "detached task survives old frontend lease without polling");
            var stale = call(a, "/ai/tasks", "POST", body("slow-stale")); String stalePath = "/ai/tasks/" + stale.path("taskId").asText();
            awaitSend(a, stalePath, "slow-stale");
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
            var ownerNoticeIds = java.util.stream.StreamSupport.stream(pending.spliterator(), false).map(notice -> notice.path("attemptId").asText()).collect(java.util.stream.Collectors.toSet());
            var otherNotices = TaskNode.JSON.readTree(call(d, "/ai/tasks/notices", "GET", "", "2").body());
            check(java.util.stream.StreamSupport.stream(otherNotices.spliterator(), false).noneMatch(notice -> ownerNoticeIds.contains(notice.path("attemptId").asText())),
                    "another actor's own notices cannot include this owner's pending completion notices");
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
