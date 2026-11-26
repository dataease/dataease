package io.dataease.ai;

import com.fasterxml.jackson.databind.JsonNode;
import io.dataease.ai.engine.conversation.AiConversationManage;
import io.dataease.extensions.datasource.dto.TableField;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import static io.dataease.ai.ToolConversationRegression.*;

/** Exercises the actual editor HTTP endpoints and checks the independently received model bodies. */
final class DatasetSqlHistoryRegression extends ConversationSocketRegression.EditorHttp {
    private static final String INSPECT = "先读取 orders 再读取 items";
    private static final String QUESTION = "步骤 1 里的 paid_at 和 created_at 各是什么类型？";
    private static final String OUTCOMES = "核对 SQL 建议、表关联和表搜索";
    final AtomicInteger authorizationChecks = new AtomicInteger();
    final AtomicInteger selectedSnapshots = new AtomicInteger();
    private volatile int snapshotsBeforeNaturalLookup;
    // The controlled model is told which structured call to make for the current test utterance.
    private record NumberedLookup(String question, String selection, String fields) { }
    private volatile NumberedLookup scriptedLookup;

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
        var names = List.of("paid_at", "created_at", "type", "step", "date", "id", "and", "include", "present", "temporal", "column,part", "#count");
        for (int i = 0; i < 80; i++) {
            var field = new TableField();
            field.setOriginName(i < names.size() ? names.get(i) : "other_column_" + i);
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
        if (question.equals("现在只补查 items 的 missing_code") || question.equals("刷新 orders 的 paid_at")) {
            if (!"tool".equals(last.path("role").asText())) {
                boolean items = question.contains("items");
                return call("read-current-fields", "get_named_table_fields", JSON.writeValueAsString(Map.of(
                        "table", items ? "items" : "orders", "fields", items ? "missing_code" : "paid_at")));
            }
            return frame(Map.of("content", "来自本轮读取"), "stop");
        }
        if (question.equals("现在读取 orders 全表结构")) {
            if (!"tool".equals(last.path("role").asText())) return call("read-orders", "get_table_fields", "{\"table\":\"orders\"}");
            return frame(Map.of("content", "已读取当前结构"), "stop");
        }
        if (question.equals("How many columns were returned?")) {
            if (!"tool".equals(last.path("role").asText())) return call("count", "find_historical_step", JSON.writeValueAsString(
                    Map.of("action", "get_table_fields", "subject", "*", "fields", "#count", "selection", "latest")));
            return frame(Map.of("content", "Counted the previous result"), "stop");
        }
        if (question.equals("刚才 orders 的时间字段") || question.equals("选择 Probe 11")) {
            if (!"tool".equals(last.path("role").asText())) {
                snapshotsBeforeNaturalLookup = selectedSnapshots.get();
                return call("lookup", "find_historical_step", JSON.writeValueAsString(
                    Map.of("action", "get_table_fields", "subject", "orders", "fields", "temporal",
                            "selection", question.contains("选择") ? "source:Probe 11" : "any")));
            }
            var record = JSON.readTree(last.path("content").asText());
            if ("AMBIGUOUS".equals(record.path("lookup").asText())) {
                StringJoiner options = new StringJoiner("\n", "要查询哪一次读取？\n", "");
                int index = 0;
                for (var candidate : record.path("candidates"))
                    options.add(++index + ". " + candidate.path("resourceName").asText() + " · 回答 " + candidate.path("answerNumber").asInt());
                return frame(Map.of("content", options.toString()), "stop");
            }
            return frame(Map.of("content", "FOUND".equals(record.path("lookup").asText()) ? "当时的时间字段" : "本对话没有记录"), "stop");
        }
        if (OUTCOMES.equals(question)) {
            return switch (last.path("tool_call_id").asText("")) {
                case "" -> call("invalid-query", "propose_sql", "{\"sql\":\"not a query\"}");
                case "invalid-query" -> call("valid-query", "propose_sql", "{\"sql\":\"SELECT 1 AS original_projection_marker\"}");
                case "valid-query" -> call("relation", "read_relations", "{\"fromTable\":\"orders\",\"toTable\":\"items\"}");
                case "relation" -> call("search", "search_physical_tables", "{\"terms\":\"unknown_search_marker\"}");
                default -> frame(Map.of("content", "已完成核对"), "stop");
            };
        }
        if (INSPECT.equals(question)) {
            boolean first = !"tool".equals(last.path("role").asText());
            return call(first ? "order-fields" : "item-fields", "get_table_fields", first ? "{\"table\":\"orders\"}" : "{\"table\":\"items\"}");
        }
        var lookup = QUESTION.equals(question) ? new NumberedLookup(QUESTION, "last.1", "paid_at,created_at") : scriptedLookup;
        if (lookup != null && lookup.question().equals(question)) {
            if (QUESTION.equals(question)) release.countDown();
            if (!"tool".equals(last.path("role").asText())) return lookupStep(lookup.selection(), lookup.fields());
            var evidence = JSON.readTree(last.path("content").asText()).path("evidence");
            if (evidence.has("status")) return frame(Map.of("content", "当时的结论是 " + evidence.path("status").asText()), "stop");
            if (QUESTION.equals(question)) {
                var answer = new ArrayList<String>();
                for (var field : evidence.path("fields")) answer.add(field.path("name").asText() + " " + field.path("type").asText());
                return frame(Map.of("content", String.join(", ", answer)), "stop");
            }
            return frame(Map.of("content", "已核对历史记录"), "stop");
        }
        return frame(Map.of("content", "好的"), "stop");
    }

    private void assertDenied(String method, String path, Object body, String user, String workspace) throws Exception {
        var response = request(method, path, body, user, workspace);
        if (response.statusCode() != 403 || response.body().contains(marker(0)) || response.body().contains("paid_at"))
            throw new AssertionError("Original scope must be required: " + response.statusCode() + " " + response.body());
    }

    private static void assertInitialQuestion(JsonNode messages) throws Exception {
        var last = messages.get(messages.size() - 1);
        var content = last.path("content").asText();
        if (!"user".equals(last.path("role").asText())
                || content.startsWith("{") && JSON.readTree(content).has("historicalStep")
                || !messages.findValues("tool_call_id").isEmpty())
            throw new AssertionError("Initial request must contain the user question without historical tools or snapshot evidence: " + messages);
    }

    private void verifyRequests(String clientName, List<JsonNode> calls, int reads, int checks, int snapshots) throws Exception {
        if (calls.size() != 2 || reads != 0 || checks < 1 || snapshots != 1)
            throw new AssertionError("Unexpected model/metadata/authorization/selected-snapshot counts: " + List.of(calls.size(), reads, checks, snapshots));
        var record = new ArrayList<Object>();
        for (int index = 0; index < calls.size(); index++) {
            var request = calls.get(index);
            var messages = request.path("messages");
            var last = messages.get(messages.size() - 1);
            boolean initial = index == 0;
            var step = initial ? JSON.createObjectNode() : JSON.readTree(last.path("content").asText());
            if (initial) {
                assertInitialQuestion(messages);
                if (request.toString().contains(marker(0)) || request.toString().contains(marker(1)))
                    throw new AssertionError("First request must let the model interpret the question before any snapshot data: " + last);
            } else {
                var evidence = step.path("evidence");
                if (!"tool".equals(last.path("role").asText()) || !"history-step".equals(last.path("tool_call_id").asText())
                        || step.path("answerNumber").asInt() != 1 || step.path("stepNumber").asInt() != 1
                        || !"COMPLETE".equals(evidence.path("coverage").asText()) || evidence.path("fields").size() != 2
                        || !request.toString().contains(marker(0)) || !request.toString().contains(marker(1)))
                    throw new AssertionError("Only the explicitly requested original evidence must reach the continuation: " + step);
            }
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

    private JsonNode turnEvidence(String session, String version, String question, List<JsonNode> sent) throws Exception {
        int before = sent.size();
        var answer = finish(startTurn(session, version, question));
        if (!"COMPLETED".equals(answer.path("status").asText()) || sent.size() != before + 2)
            throw new AssertionError(question + " must make one tool call: " + answer);
        assertInitialQuestion(sent.get(before).path("messages"));
        var messages = sent.getLast().path("messages");
        var last = messages.get(messages.size() - 1);
        if (!"tool".equals(last.path("role").asText())) throw new AssertionError(last);
        var evidence = JSON.readTree(last.path("content").asText());
        if (question.endsWith("的业务结论？") && !("当时的结论是 " + evidence.at("/evidence/status").asText())
                .equals(answer.at("/result/text").asText())) throw new AssertionError(answer);
        return evidence;
    }

    private JsonNode numberedEvidence(String session, String version, String question, String selection, String fields,
                                      List<JsonNode> sent) throws Exception {
        scriptedLookup = new NumberedLookup(question, selection, fields);
        try {
            var step = turnEvidence(session, version, question, sent);
            if (!"FOUND".equals(step.path("lookup").asText())) throw new AssertionError(step);
            return step;
        } finally {
            scriptedLookup = null;
        }
    }

    private static void assertCount(JsonNode evidence, String coverage, Integer expectedCount) {
        var expected = JSON.createObjectNode().put("coverage", coverage);
        if (expectedCount != null) expected.put("fieldCount", expectedCount);
        if (!expected.equals(evidence)) throw new AssertionError("Expected historical count " + expected + ", received " + evidence);
    }

    private void verifySynchronousLookup(String base, List<JsonNode> sent, List<String> reads) throws Exception {
        var source = ok("POST", base, Map.of("scene", "dataset-sql", "resourceId", "11", "editorId", "sync-history"));
        String session = base + "/" + source.path("sessionId").asText();
        String version = source.path("contextVersion").asText();
        secondRead = new CountDownLatch(1);
        release = new CountDownLatch(1);
        try {
            String original = startTurn(session, version, INSPECT);
            if (!secondRead.await(6, TimeUnit.SECONDS)) throw new AssertionError("Expected an incomplete first turn");
            ok("POST", original + "/stop", null);
            release.countDown();
            int readsBefore = reads.size(), first = sent.size();
            var answer = ok("POST", session + "/turns", Map.of("contextVersion", version, "text", QUESTION));
            if (!"paid_at TIMESTAMP, created_at DATETIME".equals(answer.path("text").asText()) || sent.size() != first + 2)
                throw new AssertionError("Synchronous follow-up must recover the completed step of a stopped turn: " + answer);
            var definitions = sent.get(first).path("tools");
            if (definitions.size() != 1 || !"find_historical_step".equals(definitions.at("/0/function/name").asText()))
                throw new AssertionError("The synchronous endpoint may expose only the historical lookup");
            scriptedLookup = new NumberedLookup("Le premier champ de la première étape ?", "1.1", "paid_at");
            ok("POST", session + "/turns", Map.of("contextVersion", version, "text", scriptedLookup.question()));
            var messages = sent.getLast().path("messages");
            var evidence = JSON.readTree(messages.get(messages.size() - 1).path("content").asText()).path("evidence");
            if (sent.size() != first + 4 || reads.size() != readsBefore || !"paid_at".equals(evidence.at("/fields/0/name").asText()))
                throw new AssertionError("A subsequent synchronous lookup must still reuse the original evidence");
        } finally {
            release.countDown();
            scriptedLookup = null;
            ok("DELETE", session, null);
        }
        System.out.println("PASS two synchronous history lookups after an interrupted first turn, only the historical tool, no business reads");
    }

    private void verifyNatural(String base, List<JsonNode> sent, List<String> reads) throws Exception {
        var first = ok("POST", base, Map.of("scene", "dataset-sql", "resourceId", "11", "editorId", "search-history"));
        String session = base + "/" + first.path("sessionId").asText(), version = first.path("contextVersion").asText();
        turnEvidence(session, version, "现在读取 orders 全表结构", sent);
        int count = reads.size();
        var lookup = turnEvidence(session, version, "刚才 orders 的时间字段", sent);
        if (!"FOUND".equals(lookup.path("lookup").asText()) || lookup.at("/evidence/fields").size() != 2
                || !lookup.toString().contains(marker(1)) || lookup.toString().contains(marker(2)) || reads.size() != count)
            throw new AssertionError("Natural table and type clues must select only the historical time columns without querying: " + lookup);
        var tally = turnEvidence(session, version, "How many columns were returned?", sent);
        assertCount(tally.path("evidence"), "COMPLETE", 80);
        if (reads.size() != count || sent.getLast().toString().contains(marker(0)))
            throw new AssertionError("Natural count must use only the count projection of the old read");
        var second = ok("POST", base, Map.of("scene", "dataset-sql", "resourceId", "12", "editorId", "search-history",
                "previousSessionId", first.path("sessionId").asText()));
        session = base + "/" + second.path("sessionId").asText();
        version = second.path("contextVersion").asText();
        turnEvidence(session, version, "现在读取 orders 全表结构", sent);
        count = reads.size();
        var ambiguous = turnEvidence(session, version, "刚才 orders 的时间字段", sent);
        if (!"AMBIGUOUS".equals(ambiguous.path("lookup").asText()) || ambiguous.path("candidates").size() != 2
                || ambiguous.toString().contains(marker(0)) || ambiguous.at("/candidates/0/resourceName").equals(ambiguous.at("/candidates/1/resourceName")))
            throw new AssertionError("Same table in different sources needs only authorized distinguishing labels: " + ambiguous);
        var choice = turnEvidence(session, version, "选择 Probe 11", sent);
        if (choice.path("answerNumber").asInt() != 1 || choice.at("/evidence/fields").size() != 2 || reads.size() != count)
            throw new AssertionError("Selecting the original source must use its snapshot without a query: " + choice);
        var refresh = turnEvidence(session, version, "刷新 orders 的 paid_at", sent);
        if (!"CURRENT_READ".equals(refresh.path("source").asText()) || refresh.path("fields").size() != 1
                || !refresh.toString().contains("second-source-paid") || refresh.toString().contains(marker(1))
                || !reads.subList(count, reads.size()).equals(List.of("tables", "orders")))
            throw new AssertionError("Refresh must read once from current target and expose only paid_at: " + refresh + reads.subList(count, reads.size()));
        var selected = numberedEvidence(session, version, "第1步的 paid_at 和 unrecorded_note 当时有记录吗",
                "last.1", "paid_at,unrecorded_note", sent);
        var partial = selected.path("evidence");
        if (!"PARTIAL".equals(partial.path("coverage").asText()) || !"second-source-paid".equals(partial.at("/fields/0/comment").asText())
                || !"unrecorded_note".equals(partial.at("/unrecordedFields/0").asText()) || partial.has("absentFields"))
            throw new AssertionError("A targeted refresh remains a partial snapshot for a subsequent historical question: " + partial);
        int originalAnswer = selected.path("answerNumber").asInt();
        assertCount(numberedEvidence(session, version, "第" + originalAnswer + "次回答的第1步读到了多少个字段？",
                originalAnswer + ".1", "#count", sent).path("evidence"), "PARTIAL", 1);
        ok("DELETE", session, null);
        var fresh = ok("POST", base, Map.of("scene", "dataset-sql", "resourceId", "11", "editorId", "new-history"));
        session = base + "/" + fresh.path("sessionId").asText();
        count = reads.size();
        var missing = turnEvidence(session, fresh.path("contextVersion").asText(), "刚才 orders 的时间字段", sent);
        if (!"MISSING".equals(missing.path("lookup").asText()) || missing.has("candidates") || reads.size() != count)
            throw new AssertionError("A fresh conversation cannot recover old candidates or trigger a business read: " + missing);
        ok("DELETE", session, null);
        System.out.println("PASS natural SQL history, source ambiguity and selection with zero business reads, current single-field refresh and full table refresh");
    }

    private void verifyHistoricalOutcomes(String base, List<JsonNode> sent, List<String> reads) throws Exception {
        var opened = ok("POST", base, Map.of("scene", "dataset-sql", "resourceId", "11", "editorId", "sql-outcomes"));
        String session = base + "/" + opened.path("sessionId").asText();
        String version = opened.path("contextVersion").asText();
        var original = finish(startTurn(session, version, OUTCOMES));
        if (!"SELECT 1 AS original_projection_marker".equals(original.at("/result/sql").asText()))
            throw new AssertionError("The corrected proposal must remain available to the editor: " + original);
        int readsBefore = reads.size();
        var expected = List.of("INVALID", "NOT_RUN", "NO_CONFIRMED_PATH", "NO_MATCH");
        for (int i = 0; i < expected.size(); i++) {
            var step = numberedEvidence(session, version, "第一次回答的第 " + (i + 1) + " 步的业务结论？",
                    "1." + (i + 1), "*", sent);
            var request = sent.getLast();
            if (!"SUCCEEDED".equals(step.path("status").asText())
                    || !step.path("evidence").equals(JSON.valueToTree(Map.of("status", expected.get(i))))
                    || reads.size() != readsBefore || request.toString().contains("original_projection_marker")
                    || request.toString().contains("unknown_search_marker"))
                throw new AssertionError("Expected only the original business outcome " + expected.get(i) + ": " + step);
        }
        ok("DELETE", session, null);
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
            var spelling = numberedEvidence(session, version, "Les colonnes paid_at et PAID_AT existent-elles ?",
                    "1.1", "paid_at,PAID_AT", sent).path("evidence");
            if (!spelling.path("fields").findValuesAsText("name").equals(List.of("paid_at"))
                    || !spelling.path("absentFields").equals(JSON.valueToTree(List.of("PAID_AT"))))
                throw new AssertionError("Physical names in a case-sensitive snapshot retain their exact spelling: " + spelling);
            record Example(String question, String fields, List<String> names) { }
            for (var query : List.of(
                    new Example("第一次回答的第 1 步中有哪些时间字段？", "temporal", List.of("paid_at", "created_at")),
                    new Example("Quels champs temporels ont été lus à la première étape de la première réponse ?",
                            "temporal", List.of("paid_at", "created_at")),
                    new Example("What are the types of type and id in step 1 of the first answer?", "type,id", List.of("type", "id")),
                    new Example("In step 1 of the first answer, describe the columns `and` and `step`.", "and,step", List.of("step", "and")),
                    new Example("Is the date column in step 1 of the first answer an integer?", "date", List.of("date")),
                    new Example("最初の回答で temporal と column,part と #count の型は何でしたか？",
                            JSON.writeValueAsString(List.of("temporal", "column,part", "#count")), List.of("temporal", "column,part", "#count")))) {
                var evidence = numberedEvidence(session, version, query.question(), "1.1", query.fields(), sent).path("evidence");
                if (!evidence.path("fields").findValuesAsText("name").equals(query.names()) || reads.size() != count || evidence.has("absentFields"))
                    throw new AssertionError("Unexpected historical fields for " + query.question() + ": " + evidence);
            }
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
            var historical = numberedEvidence(session, version, "第一次回答的第 1 步里是否有 missing_code 和 paid_at？",
                    "1.1", "missing_code,paid_at", sent).path("evidence");
            if (historical.path("fields").size() != 1 || !historical.toString().contains(marker(0))
                    || !historical.path("absentFields").equals(JSON.valueToTree(List.of("missing_code"))))
                throw new AssertionError("A complete read records the absence of the named column: " + historical);
            var partial = numberedEvidence(session, version, "第一次回答的第 2 步有记录 missing_code 吗？",
                    "1.2", "missing_code", sent);
            if (!"CANCELLED".equals(partial.path("status").asText()) || !"PARTIAL".equals(partial.at("/evidence/coverage").asText())
                    || partial.at("/evidence/fields").size() != 0 || !"missing_code".equals(partial.at("/evidence/unrecordedFields/0").asText())
                    || !partial.at("/evidence/absentFields").isMissingNode())
                throw new AssertionError("A stopped read cannot prove column absence: " + partial);
            assertCount(numberedEvidence(session, version, "How many columns were returned in step 1 of the first answer?",
                    "1.1", "#count", sent).path("evidence"), "COMPLETE", 80);
            assertCount(numberedEvidence(session, version, "第一次回答的第2步读到了多少个字段？",
                    "1.2", "#count", sent).path("evidence"), "PARTIAL", null);
            var combined = numberedEvidence(session, version,
                    "Combien de colonnes à la première étape de la première réponse, et quels étaient leurs noms ?",
                    "1.1", "#count,#names", sent).path("evidence");
            if (combined.path("fieldCount").asInt() != 80 || combined.path("fieldNames").size() != 80
                    || !"paid_at".equals(combined.at("/fieldNames/0").asText()) || !"other_column_79".equals(combined.at("/fieldNames/79").asText())
                    || combined.has("fields") || sent.getLast().toString().contains(marker(0)) || reads.size() != count)
                throw new AssertionError("Compound projection must return both recorded count and names without full field details: " + combined);
            var countAndField = numberedEvidence(session, version, "一共有几列？其中 paid_at 的类型是什么？",
                    "1.1", "#count,paid_at", sent).path("evidence");
            if (countAndField.path("fieldCount").asInt() != 80 || countAndField.path("fields").size() != 1
                    || !"TIMESTAMP".equals(countAndField.at("/fields/0/type").asText()) || countAndField.has("fieldNames")
                    || sent.getLast().toString().contains(marker(1)) || reads.size() != count)
                throw new AssertionError("Counting should preserve the requested field details: " + countAndField);
            var absent = numberedEvidence(session, version, "Does step 1 of the first answer include price?",
                    "1.1", "price", sent).path("evidence");
            var unrecorded = numberedEvidence(session, version, "第 1 次回答的第 2 步有记录 price 吗？",
                    "1.2", "price", sent).path("evidence");
            if (!absent.path("absentFields").equals(JSON.valueToTree(List.of("price"))) || !absent.path("fields").isEmpty()
                    || !unrecorded.path("unrecordedFields").equals(JSON.valueToTree(List.of("price"))) || reads.size() != count)
                throw new AssertionError("Named field coverage must retain the difference between complete and partial reads");
            var supplemental = turnEvidence(session, version, "现在只补查 items 的 missing_code", sent);
            if (!"CURRENT_READ".equals(supplemental.path("source").asText())
                    || !supplemental.path("absentFields").toString().equals("[\"missing_code\"]")
                    || !reads.subList(count, reads.size()).equals(List.of("tables", "items")))
                throw new AssertionError("The incomplete old read should be supplemented only on the requested table: " + supplemental);
            count = reads.size();
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
            verifyNatural(base, sent, reads);
            verifyHistoricalOutcomes(base, sent, reads);
            verifySynchronousLookup(base, sent, reads);
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
                    if (sent.size() != browserCalls + 6) throw new AssertionError("Expected two original, two numbered and two natural lookup calls");
                    verifyRequests("chrome", List.copyOf(sent.subList(browserCalls + 2, browserCalls + 4)), reads.size() - count,
                            authorizationChecks.get() - checks, snapshotsBeforeNaturalLookup - snapshots);
                    var messages = sent.getLast().path("messages");
                    var lookup = JSON.readTree(messages.get(messages.size()-1).path("content").asText());
                    if (!"FOUND".equals(lookup.path("lookup").asText()) || lookup.at("/evidence/fields").size() != 2
                            || lookup.toString().contains(marker(2)))
                        throw new AssertionError("Formal SQL editor did not reuse the two original temporal fields: " + lookup);

                } finally { if (process.isAlive()) process.destroyForcibly(); }
            }
        } finally { release.countDown(); allowed.set(true); service.stop(); service.destroy(); }
    }
}
