package io.dataease.ai;

import com.fasterxml.jackson.databind.JsonNode;
import io.dataease.ai.engine.conversation.AiConversationManage;
import io.dataease.extensions.datasource.dto.TableField;

import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import static io.dataease.ai.ToolConversationRegression.*;

/** Exercises the actual editor HTTP endpoints and checks the independently received model bodies. */
final class DatasetSqlHistoryRegression {
    private static final String INSPECT = "先读取 orders 再读取 items";
    private static final String QUESTION = "步骤 1 里的 paid_at 和 created_at 各是什么类型？";
    private final HttpClient client = HttpClient.newHttpClient();
    final AtomicInteger authorizationChecks = new AtomicInteger();
    final AtomicInteger selectedSnapshots = new AtomicInteger();
    private volatile CountDownLatch secondRead = new CountDownLatch(1);
    private volatile CountDownLatch release = new CountDownLatch(1);

    static String marker(int index) { return "column-marker-%02d!".formatted(index); }
    List<TableField> fields(String table) throws InterruptedException {
        if ("items".equals(table)) {
            secondRead.countDown();
            release.await(30, TimeUnit.SECONDS);
            return List.of();
        }
        if (!"orders".equals(table)) throw new AssertionError(table);
        var fields = new ArrayList<TableField>();
        for (int i = 0; i < 80; i++) {
            var field = new TableField();
            field.setOriginName(i == 0 ? "paid_at" : i == 1 ? "created_at" : "other_column_" + i);
            field.setName(marker(i));
            field.setFieldType(i == 0 ? "TIMESTAMP" : i == 1 ? "DATETIME" : "INTEGER");
            fields.add(field);
        }
        return fields;
    }

    String respond(JsonNode request) throws Exception {
        var messages = request.path("messages");
        var last = messages.get(messages.size() - 1);
        JsonNode user = null;
        for (var message : messages) if ("user".equals(message.path("role").asText())) user = message;
        String latest = Objects.requireNonNull(user).path("content").asText();
        JsonNode envelope = latest.startsWith("{") ? JSON.readTree(latest) : JSON.createObjectNode();
        String question = envelope.path("userMessage").asText(latest);
        if (INSPECT.equals(question)) {
            boolean first = !"tool".equals(last.path("role").asText());
            return call(first ? "order-fields" : "item-fields", "get_table_fields", first ? "{\"table\":\"orders\"}" : "{\"table\":\"items\"}");
        }
        if (QUESTION.equals(question)) {
            release.countDown();
            if (!"tool".equals(last.path("role").asText())) return call("current-guide", "read_sql_guide", "{\"topic\":\"projection\"}");
            var selected = envelope.at("/historicalStep/evidence/fields");
            var answer = new ArrayList<String>();
            for (var field : selected) answer.add(field.path("name").asText() + " " + field.path("type").asText());
            return frame(Map.of("content", answer.isEmpty() ? "缺少原记录" : String.join(", ", answer)), "stop");
        }
        return frame(Map.of("content", "好的"), "stop");
    }

    private HttpResponse<String> request(String method, String path, Object body, String user, String workspace) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create(path)).header("X-Test-Identity", user)
                .header("X-DE-Error-Contract", "2").header("Content-Type", "application/json");
        if (workspace != null) builder.header("X-Test-Scope", workspace);
        return client.send(builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofByteArray(JSON.writeValueAsBytes(body))).build(), HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode ok(String method, String path, Object body) throws Exception {
        var response = request(method, path, body, "writer", null);
        if (response.statusCode() != 200) throw new AssertionError(method + " " + path + " -> " + response.statusCode() + " " + response.body());
        return JSON.readTree(response.body());
    }

    private String startTurn(String session, String version, String text) throws Exception {
        String path = session + "/executions";
        return path + "/" + ok("POST", path, Map.of("contextVersion", version, "text", text)).path("executionId").asText();
    }

    private JsonNode finish(String run) throws Exception {
        for (int i = 0; i < 240; i++) {
            var events = ok("GET", run + "/events?after=0", null);
            if (!"RUNNING".equals(events.path("status").asText())) return events;
            Thread.sleep(20);
        }
        throw new AssertionError("no terminal event for " + run);
    }

    private void assertDenied(String method, String path, Object body, String user, String workspace) throws Exception {
        var response = request(method, path, body, user, workspace);
        if (response.statusCode() != 403 || response.body().contains(marker(0)) || response.body().contains("paid_at"))
            throw new AssertionError("Original scope must be required: " + response.statusCode() + " " + response.body());
    }

    private void verifyRequests(String clientName, List<JsonNode> calls, int reads, int checks, int snapshots) throws Exception {
        if (calls.size() != 2 || reads != 0 || checks < 1 || snapshots != 1)
            throw new AssertionError("Unexpected model/metadata/authorization/selected-snapshot counts: " + List.of(calls.size(), reads, checks, snapshots));
        var record = new ArrayList<Object>();
        for (var request : calls) {
            var messages = request.path("messages");
            var latest = messages.findParents("role").stream().filter(m -> "user".equals(m.path("role").asText())).reduce((a,b) -> b).orElseThrow();
            var step = JSON.readTree(latest.path("content").asText()).path("historicalStep");
            var evidence = step.path("evidence");
            if (step.path("answerNumber").asInt() != 1 || step.path("stepNumber").asInt() != 1
                    || !"COMPLETE".equals(evidence.path("coverage").asText()) || evidence.path("fields").size() != 2
                    || !request.toString().contains(marker(0)) || !request.toString().contains(marker(1)))
                throw new AssertionError("Both and only requested physical fields must reach every upstream request: " + step);
            for (int i = 2; i < 80; i++) if (request.toString().contains(marker(i)))
                throw new AssertionError("Unrelated field marker " + i + " reached upstream");
            if (request.toString().contains("order-fields") || request.toString().contains("item-fields"))
                throw new AssertionError("Historical call protocol leaked");
            record.add(Map.of("roles", messages.findValuesAsText("role"), "historicalStep", step));
        }
        var result = Map.of("client", clientName, "requests", record, "businessReads", reads,
                "datasourceReadPermissionChecks", checks, "selectedSessionSnapshotReads", snapshots, "excludedFieldMarkers", 78);
        System.out.println("PASS SQL history " + JSON.writeValueAsString(result));
        String directory = System.getenv("AI_SQL_HISTORY_EVIDENCE");
        if (directory != null) Files.writeString(Path.of(directory, clientName + ".json"), JSON.writeValueAsString(Map.of("observations", result, "bodies", calls)));
    }

    void verify(AiConversationManage manage, List<JsonNode> sent, List<String> reads, AtomicBoolean allowed) throws Exception {
        var service = ConversationSocketRegression.editorServer(manage);
        try {
            service.start();
            String base = "http://127.0.0.1:" + service.getPort() + "/de2api/ai/conversations";
            var source = ok("POST", base, Map.of("scene", "dataset-sql", "resourceId", "11", "editorId", "history-http"));
            String session = base + "/" + source.path("sessionId").asText();
            String version = source.path("contextVersion").asText();
            String original = startTurn(session, version, INSPECT);
            if (!secondRead.await(6, TimeUnit.SECONDS)) throw new AssertionError("Second tool read did not start: " + ok("GET", original + "/events?after=0", null));
            var stopped = ok("POST", original + "/stop", null);
            release.countDown();
            if (!"CANCELLED".equals(stopped.path("status").asText())) throw new AssertionError(stopped);
            var originalStep = finish(original).path("events").findParents("callId").stream()
                    .filter(t -> t.path("stepNumber").asInt() == 1 && "SUCCEEDED".equals(t.path("status").asText())).findFirst().orElseThrow();
            String detail = original + "/tools/" + originalStep.path("resultSequence").asLong();
            String visible = ok("GET", detail, null).path("result").asText();
            if (JSON.readTree(visible).path("fields").size() != 80 || !visible.contains(marker(79)) || sent.size() != 2
                    || !sent.getLast().toString().contains(marker(79))) throw new AssertionError("Full current-turn protocol and editor detail required");
            int checks = authorizationChecks.get(), snapshots = selectedSnapshots.get(), count = reads.size(), first = sent.size();
            var answered = finish(startTurn(session, version, QUESTION));
            verifyRequests("http", List.copyOf(sent.subList(first, sent.size())), reads.size() - count,
                    authorizationChecks.get() - checks, selectedSnapshots.get() - snapshots);
            if (!"paid_at TIMESTAMP, created_at DATETIME".equals(answered.at("/result/text").asText())) throw new AssertionError(answered);
            finish(startTurn(session, version, "请解释 SQL"));
            String unrelated = sent.getLast().toString();
            var unrelatedMessages = sent.getLast().path("messages");
            boolean replaysStep = false;
            for (var message : unrelatedMessages) {
                String text = message.path("content").asText();
                if ("user".equals(message.path("role").asText()) && text.startsWith("{") && JSON.readTree(text).has("historicalStep")) replaysStep = true;
            }
            if (unrelated.contains(marker(0)) || replaysStep || unrelated.contains("tool_call_id"))
                throw new AssertionError("Unrelated question must not replay past evidence or calls");
            for (String question : List.of("第一次回答的第 1 步中有哪些时间字段？", "第一次回答的第 1 步里是否有 missing_code 和 paid_at？")) {
                finish(startTurn(session, version, question));
                var messages = sent.getLast().path("messages");
                var historical = JSON.readTree(messages.get(messages.size() - 1).path("content").asText()).at("/historicalStep/evidence");
                if (question.contains("时间字段")) {
                    if (historical.path("fields").size() != 2 || !historical.toString().contains(marker(1)) || historical.toString().contains(marker(2)))
                        throw new AssertionError("Historical temporal fields must use the original types: " + historical);
                } else if (historical.path("fields").size() != 1 || !historical.toString().contains(marker(0))
                        || !historical.path("absentFields").toString().equals("[\"missing_code\"]"))
                    throw new AssertionError("A complete read records the absence of the named column: " + historical);
            }
            finish(startTurn(session, version, "第一次回答的第 2 步有记录 missing_code 吗？"));
            var partialMessages = sent.getLast().path("messages");
            var partial = JSON.readTree(partialMessages.get(partialMessages.size() - 1).path("content").asText()).path("historicalStep");
            if (!"CANCELLED".equals(partial.path("status").asText()) || !"PARTIAL".equals(partial.at("/evidence/coverage").asText())
                    || partial.at("/evidence/fields").size() != 0 || !"missing_code".equals(partial.at("/evidence/unrecordedFields/0").asText())
                    || !partial.at("/evidence/absentFields").isMissingNode())
                throw new AssertionError("A stopped read cannot prove column absence: " + partial);
            int beforeDeny = sent.size();
            for (String scope : List.of("foreign", "workspace", "revoked")) {
                allowed.set(!"revoked".equals(scope));
                String actor = "foreign".equals(scope) ? "other" : "writer";
                String workspace = "workspace".equals(scope) ? scope : null;
                assertDenied("GET", detail, null, actor, workspace);
                assertDenied("POST", session + "/executions", Map.of("contextVersion", version, "text", "第一次回答的第 1 步里的 paid_at"), actor, workspace);
            }
            if (reads.size() != count || sent.size() != beforeDeny) throw new AssertionError("Denied history reached model or datasource");
            allowed.set(true);
            ok("DELETE", session, null);
            if ("1".equals(System.getenv("AI_SQL_HISTORY_BROWSER"))) {
                secondRead = new CountDownLatch(1); release = new CountDownLatch(1);
                int browserCalls = sent.size();
                var browser = new ProcessBuilder("node", "node_modules/@playwright/test/cli.js", "test",
                        "-c", "playwright.dataset-sql-assistant.config.cjs", "dataset-sql-assistant-history.spec.cjs");
                browser.directory(new java.io.File("tests/browser"));
                browser.environment().put("DATASET_ASSISTANT_BACKEND", "http://127.0.0.1:" + service.getPort());
                var process = browser.inheritIO().start();
                try {
                    if (!secondRead.await(55, TimeUnit.SECONDS)) throw new AssertionError("Chrome did not start the second field read");
                    count = reads.size(); checks = authorizationChecks.get(); snapshots = selectedSnapshots.get();
                    if (!process.waitFor(60, TimeUnit.SECONDS) || process.exitValue() != 0)
                        throw new AssertionError("Chrome editor did not complete the historical follow-up");
                    if (sent.size() != browserCalls + 4) throw new AssertionError("Expected two original and two follow-up calls");
                    verifyRequests("chrome", List.copyOf(sent.subList(browserCalls + 2, sent.size())), reads.size() - count,
                            authorizationChecks.get() - checks, selectedSnapshots.get() - snapshots);
                } finally { if (process.isAlive()) process.destroyForcibly(); }
            }
        } finally { release.countDown(); allowed.set(true); service.stop(); service.destroy(); }
    }
}
