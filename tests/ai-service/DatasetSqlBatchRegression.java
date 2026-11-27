package io.dataease.ai;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static io.dataease.ai.ToolConversationRegression.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/** Exercise providers that stream several calls despite parallel_tool_calls=false. */
final class DatasetSqlBatchRegression {
    private record Call(String id, String name, String key, String value) {
        String arguments() throws Exception { return JSON.writeValueAsString(Map.of(key, value)); }
    }
    private static final Call REGISTERED = new Call("registered", "search_registered_tables", "terms", "用户,用户表,user");
    private static final Call PHYSICAL = new Call("physical", "search_physical_tables", "terms", "用户,user");
    private static final Call FIELDS = new Call("fields", "get_table_fields", "table", "per_user");
    private static final Call INVALID_FIELDS = new Call(FIELDS.id(), FIELDS.name(), "wrong_key", "per_user");
    private static final Call GUIDE = new Call("guide", "read_sql_guide", "topic", "projection");
    private static final Call OFFER = new Call("early", "propose_sql", "sql", "SELECT id FROM per_user");

    private static String fragments(List<Call> calls) throws Exception {
        var starts = new ArrayList<Map<String, Object>>();
        var ends = new ArrayList<Map<String, Object>>();
        for (int index = 0; index < calls.size(); index++) {
            var call = calls.get(index);
            String args = call.arguments();
            int split = args.length() / 2;
            starts.add(Map.of("index", index, "id", call.id(), "type", "function",
                    "function", Map.of("name", call.name(), "arguments", args.substring(0, split))));
            ends.addFirst(Map.of("index", index, "function", Map.of("arguments", args.substring(split))));
        }
        return frame(Map.of("tool_calls", starts), "").replace("\"finish_reason\":\"\"", "\"finish_reason\":null")
                + frame(Map.of("tool_calls", ends), "tool_calls");
    }

    private static List<Call> calls(String mode) {
        return switch (mode) {
            case "batch-discover" -> List.of(REGISTERED, PHYSICAL);
            case "batch-reversed" -> List.of(PHYSICAL, REGISTERED);
            case "batch-read" -> List.of(FIELDS, GUIDE);
            case "batch-early", "batch-resubmit" -> List.of(OFFER, FIELDS);
            case "batch-correct" -> List.of(INVALID_FIELDS, GUIDE);
            case "batch-reuse-executed" -> List.of(FIELDS, GUIDE);
            case "batch-stop" -> List.of(REGISTERED, PHYSICAL, GUIDE);
            case "batch-duplicate-id" -> List.of(FIELDS, new Call(FIELDS.id(), GUIDE.name(), GUIDE.key(), GUIDE.value()));
            default -> throw new IllegalArgumentException(mode);
        };
    }

    static String respond(String mode, JsonNode body) throws Exception {
        var messages = body.path("messages");
        var last = messages.get(messages.size() - 1);
        if ("user".equals(last.path("role").asText())) {
            if ("batch-history".equals(mode)) return lookupStep("1.1", "*");
            return fragments(calls(mode));
        }
        String lastId = last.path("tool_call_id").asText();
        if ("batch-resubmit".equals(mode) && FIELDS.id().equals(lastId))
            return call(OFFER.id(), OFFER.name(), OFFER.arguments());
        if ("batch-correct".equals(mode) && GUIDE.id().equals(lastId))
            return call(FIELDS.id(), FIELDS.name(), FIELDS.arguments());
        if ("batch-reuse-executed".equals(mode) && GUIDE.id().equals(lastId))
            return call(FIELDS.id(), PHYSICAL.name(), PHYSICAL.arguments());
        return frame(Map.of("content", "已核对本轮工具结果。"), "stop");
    }

    private static JsonNode openEditor(MockMvc mvc, String editor) throws Exception {
        return JSON.readTree(mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsBytes(Map.of("scene", "dataset-sql", "resourceId", "11", "editorId", editor, "sql", ""))))
                .andReturn().getResponse().getContentAsString());
    }

    private static Map<String, JsonNode> results(JsonNode request, List<Call> expected) throws Exception {
        var messages = request.path("messages");
        if (messages.size() != 3 + expected.size() || !"assistant".equals(messages.get(2).path("role").asText()))
            throw new AssertionError("A single assistant message must carry the entire batch: " + messages);
        var offered = messages.get(2).path("tool_calls");
        if (offered.size() != expected.size()) throw new AssertionError("Incomplete batch replay: " + offered);
        var results = new HashMap<String, JsonNode>();
        for (int i = 0; i < expected.size(); i++) {
            var call = expected.get(i);
            var replay = offered.get(i);
            if (!call.id().equals(replay.path("id").asText()) || !call.name().equals(replay.at("/function/name").asText())
                    || !call.arguments().equals(replay.at("/function/arguments").asText()))
                throw new AssertionError("Interleaved arguments or provider IDs lost their index: " + replay);
            var result = messages.get(3 + i);
            if (!"tool".equals(result.path("role").asText()) || results.put(result.path("tool_call_id").asText(),
                    JSON.readTree(result.path("content").asText())) != null) throw new AssertionError("Invalid tool result: " + result);
        }
        if (!results.keySet().equals(new HashSet<>(expected.stream().map(Call::id).toList())))
            throw new AssertionError("Each provider call needs exactly one corresponding result: " + results);
        return results;
    }

    static void verify(MockMvc mvc, AtomicReference<String> mode, List<JsonNode> sent, List<String> reads,
                       AtomicReference<CountDownLatch> entered, AtomicReference<CountDownLatch> release) throws Exception {
        for (String scenario : List.of("batch-discover", "batch-reversed", "batch-read", "batch-early", "batch-resubmit", "batch-duplicate-id")) {
            sent.clear(); reads.clear(); mode.set(scenario);
            var session = openEditor(mvc, scenario);
            var expected = calls(scenario);
            boolean discover = expected.contains(PHYSICAL);
            var page = terminal(mvc, start(mvc, session, discover ? "查询所有用户" : "查询 per_user 的 id"));
            if (!sent.getFirst().path("parallel_tool_calls").isBoolean() || sent.getFirst().get("parallel_tool_calls").asBoolean())
                throw new AssertionError("The request must retain the existing provider setting");
            if ("batch-duplicate-id".equals(scenario)) {
                if (!"FAILED".equals(page.path("status").asText()) || !reads.isEmpty() || sent.size() != 1)
                    throw new AssertionError("Duplicate provider IDs must fail before metadata reads: " + page);
            } else {
                boolean resubmit = "batch-resubmit".equals(scenario);
                if (!"COMPLETED".equals(page.path("status").asText()) || sent.size() != (resubmit ? 3 : 2))
                    throw new AssertionError(scenario + " failed to continue exactly once per batch: " + page);
                var results = results(sent.get(1), expected);
                if (discover) {
                    var completed = page.path("events").findParents("callId").stream()
                            .filter(t -> "SUCCEEDED".equals(t.path("status").asText())).toList();
                    if (!"per_user".equals(results.get("registered").at("/tables/0/table").asText())
                            || !"per_user".equals(results.get("physical").at("/candidates/0/table").asText())
                            || !reads.equals(List.of("tables")) || completed.size() != 2
                            || !completed.get(0).path("callId").asText().equals("registered") || completed.get(0).path("stepNumber").asInt() != 1
                            || !completed.get(1).path("callId").asText().equals("physical") || completed.get(1).path("stepNumber").asInt() != 2)
                        throw new AssertionError("Registered and physical candidates must retain their source and execution order: " + results + " " + completed);
                } else {
                    if (!"id".equals(results.get("fields").at("/fields/0/name").asText()) || !reads.equals(List.of("tables", "per_user")))
                        throw new AssertionError("Named-table lookup must use current authorized fields: " + results + " " + reads);
                    if (expected.contains(GUIDE) && !"projection".equals(results.get("guide").path("topic").asText()))
                        throw new AssertionError("Guide result belongs to its own ID: " + results);
                    if (expected.contains(OFFER) && (!"DEFERRED".equals(results.get("early").path("status").asText())
                            || (resubmit ? !OFFER.value().equals(page.at("/result/sql").asText()) : !page.at("/result/sql").isNull())))
                        throw new AssertionError("Only a separate model proposal after the reads can produce SQL: " + page);
                }
            }
            mvc.perform(delete("/ai/conversations/" + session.path("sessionId").asText()));
        }

        for (String scenario : List.of("batch-correct", "batch-reuse-executed")) {
            sent.clear(); reads.clear(); mode.set(scenario);
            var session = openEditor(mvc, scenario);
            var page = terminal(mvc, start(mvc, session, "查询 per_user 的 id"));
            boolean correction = "batch-correct".equals(scenario);
            if (sent.size() != (correction ? 3 : 2) || !reads.equals(List.of("tables", "per_user")))
                throw new AssertionError("Only the valid field read should execute: " + scenario + " " + reads + " " + page);
            var first = results(sent.get(1), calls(scenario)).get(FIELDS.id());
            var executed = page.path("events").findParents("callId").stream()
                    .filter(t -> FIELDS.id().equals(t.path("callId").asText()) && "STARTED".equals(t.path("status").asText())).toList();
            if (executed.size() != 1 || !FIELDS.name().equals(executed.getFirst().path("name").asText()))
                throw new AssertionError("This ID must identify exactly one real execution: " + executed);
            if (correction) {
                var messages = sent.getLast().path("messages");
                var corrected = messages.get(messages.size() - 1);
                if (!"INVALID".equals(first.path("status").asText()) || !"COMPLETED".equals(page.path("status").asText())
                        || !FIELDS.id().equals(corrected.path("tool_call_id").asText())
                        || !"id".equals(JSON.readTree(corrected.path("content").asText()).at("/fields/0/name").asText()))
                    throw new AssertionError("Corrected arguments can reuse the unexecuted provider ID: " + page);
            } else if (!"id".equals(first.at("/fields/0/name").asText()) || !"FAILED".equals(page.path("status").asText())
                    || !page.toString().contains("output_limit")) {
                throw new AssertionError("An already executed ID cannot be assigned to another tool: " + page);
            }
            mvc.perform(delete("/ai/conversations/" + session.path("sessionId").asText()));
        }

        sent.clear(); reads.clear(); mode.set("batch-stop");
        entered.set(new CountDownLatch(1)); release.set(new CountDownLatch(1));
        var session = openEditor(mvc, "batch-stop");
        String run = start(mvc, session, "查询所有用户");
        try {
            if (!entered.get().await(5, TimeUnit.SECONDS)) throw new AssertionError("Physical metadata was never reached");
            mvc.perform(post(run + "/stop"));
        } finally { release.get().countDown(); }
        var stopped = terminal(mvc, run);
        var steps = stopped.path("events").findParents("callId");
        if (!"CANCELLED".equals(stopped.path("status").asText()) || sent.size() != 1
                || steps.stream().noneMatch(t -> "registered".equals(t.path("callId").asText()) && "SUCCEEDED".equals(t.path("status").asText()))
                || steps.stream().noneMatch(t -> "physical".equals(t.path("callId").asText()) && "CANCELLED".equals(t.path("status").asText()))
                || steps.stream().anyMatch(t -> "guide".equals(t.path("callId").asText())))
            throw new AssertionError("Stop must preserve completed steps and suppress the rest of the batch: " + stopped);
        sent.clear(); reads.clear(); mode.set("batch-history");
        var history = terminal(mvc, start(mvc, session, "第一次回答的步骤 1 完成了吗？"));
        var messages = sent.getLast().path("messages");
        var historical = JSON.readTree(messages.get(messages.size() - 1).path("content").asText());
        if (!"COMPLETED".equals(history.path("status").asText()) || !"SUCCEEDED".equals(historical.path("status").asText())
                || historical.path("stepNumber").asInt() != 1 || !reads.isEmpty() || sent.size() != 2)
            throw new AssertionError("A completed member of a stopped batch must be available by its step number: " + historical);
        mvc.perform(delete("/ai/conversations/" + session.path("sessionId").asText()));
        sent.clear(); reads.clear();
        System.out.println("PASS multi-call dataset SQL discovery, named-table fields and guide, serial steps, correlated continuations, same-ID correction and deferred proposals, executed-ID rejection, stop and history");
    }
}
