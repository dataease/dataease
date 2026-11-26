package io.dataease.ai;

import com.fasterxml.jackson.databind.JsonNode;
import io.dataease.ai.engine.conversation.AiConversationManage;

import java.util.*;

import static io.dataease.ai.ToolConversationRegression.*;

/** Chrome uses both production conversation scenes. The provider can only answer from its received request. */
final class EditorCompressionRegression {
    static final String SQL = "SELECT ID FROM AI_PROBE", FORMULA = "[Price] * 2";
    static String respond(JsonNode request) throws Exception {
        var messages = request.path("messages");
        var last = messages.get(messages.size() - 1);
        var latest = messages.findParents("role").stream().filter(m -> "user".equals(m.path("role").asText()))
                .reduce((first, second) -> second).orElseThrow().path("content").asText();
        var envelope = latest.startsWith("{") ? JSON.readTree(latest) : JSON.createObjectNode();
        String question = envelope.path("userMessage").asText(latest);
        String system = messages.get(0).path("content").asText();
        boolean calc = system.contains("计算字段编辑器");
        String reply = "继续";
        if ("按已付款订单生成建议".equals(question)) {
            String lastCall = last.path("tool_call_id").asText();
            if (lastCall.isEmpty()) return calc
                    ? call("old-validation", "propose_calc_field", JSON.writeValueAsString(Map.of("expression", FORMULA, "name", "双倍价格", "groupType", "q", "deType", "3")))
                    : call("old-schema", "get_table_fields", "{\"table\":\"AI_PROBE\"}");
            if (lastCall.equals("old-schema")) return call("old-sql", "propose_sql", JSON.writeValueAsString(Map.of("sql", SQL)));
            reply = "已生成建议";
        } else if (question.equals("Confirmed: exclude chargebacks. 接下来都用英文。")) {
            reply = "Which convention?\n1. UNSELECTED_GROSS_MARKER\n2. Paid orders only";
        } else if (question.startsWith("第一次回答") || question.equals("现在呢？")) {
            if (messages.size() > 8 || !envelope.path("earlierDialogue").isArray()
                    || !envelope.path("earlierDialogue").toString().contains("exclude chargebacks")
                    || !envelope.path("earlierDialogue").toString().contains("Paid orders only")
                    || !envelope.path("earlierDialogue").toString().contains("接下来都用英文")
                    || !envelope.path("earlierDialogue").toString().contains("用繁體中文回答")
                    || messages.toString().contains("UNSELECTED_GROSS_MARKER") || messages.toString().contains("这次用日文")
                    || messages.toString().contains("old-schema") || messages.toString().contains("old-validation")
                    || messages.toString().contains("boundExpression") || messages.toString().contains("tool_call_id"))
                return frame(Map.of("content", "历史未压缩或口径丢失"), "stop");
            var selected = envelope.path("selectedSuggestion");
            var basis = envelope.path("generationBasis");
            if (selected.path("number").asInt() != 1 || !(calc ? FORMULA : SQL).equals(selected.path("text").asText()))
                reply = "缺少作者选定的建议";
            else if (question.startsWith("第一次回答")) {
                var evidence = envelope.at("/historicalStep/evidence");
                boolean found = calc ? FORMULA.equals(evidence.path("expression").asText()) && "DETAIL_PASS".equals(evidence.path("status").asText())
                        : "ID".equals(evidence.at("/fields/0/name").asText()) && evidence.path("fields").size() == 1;
                reply = found && "DRAFT".equals(basis.path("kind").asText()) && system.contains(calc ? "[Price] + 77" : "SELECT 77")
                        ? "原步驟正確，沿用已確認的口徑" : "缺少原步骤或新草稿";
            } else {
                reply = "SUGGESTION".equals(basis.path("kind").asText()) && basis.path("number").asInt() == 1
                        && !basis.path("matchesDraft").asBoolean(true) && system.contains(calc ? FORMULA : SQL)
                        ? "本輪依據為建議一，草稿另有修改" : "本轮依据不清楚";
            }
        } else if (question.equals("仅沿用已确认口径")) {
            reply = "NONE".equals(envelope.at("/generationBasis/kind").asText()) && !envelope.has("selectedSuggestion")
                    && envelope.path("earlierDialogue").toString().contains("exclude chargebacks")
                    ? "只沿用已確認口徑" : "空依据带入了旧建议";
        } else if (question.equals("换个话题，解释采购")) {
            String past = envelope.path("earlierDialogue").toString();
            reply = past.contains("接下来都用英文") && past.contains("用繁體中文回答") && !past.contains("这次用日文")
                    && system.contains("跨话题沿用") ? "Purchase definition" : "Missing conversation preference";
        } else if (question.equals("新会话的步骤 1？")) {
            reply = !envelope.has("earlierDialogue") && !envelope.has("selectedSuggestion")
                    && "MISSING".equals(envelope.at("/historicalStep/lookup").asText()) ? "新对话无此步骤" : "新对话带入旧依据";
        }
        return frame(Map.of("content", reply), "stop");
    }

    static void verify(AiConversationManage manage, List<JsonNode> requests) throws Exception {
        int first = requests.size();
        var service = ConversationSocketRegression.editorServer(manage);
        try {
            service.start();
            ConversationSocketRegression.browserSuite(service.getPort(), "dataset-calc-assistant-compression.spec.cjs");
            int calls = requests.size() - first;
            if (calls != 35) throw new AssertionError("Expected exactly 35 accepted model calls for the two editor scenarios: " + calls);
            var evidenceFile = System.getenv("AI_EDITOR_COMPRESSION_EVIDENCE");
            if (evidenceFile != null) {
                var evidence = new ArrayList<Object>();
                for (var call : requests.subList(first, requests.size())) {
                    var messages = call.path("messages");
                    var last = messages.get(messages.size() - 1);
                    String text = last.path("content").asText();
                    if (!"user".equals(last.path("role").asText()) || !text.startsWith("{")) continue;
                    var envelope = JSON.readTree(text);
                    if (envelope.has("historicalStep") || "现在呢？".equals(envelope.path("userMessage").asText())) {
                        evidence.add(Map.of("roles", messages.findValuesAsText("role"),
                                "scene", messages.get(0).path("content").asText().contains("计算字段编辑器") ? "dataset-calc-field" : "dataset-sql",
                                "user", envelope));
                    }
                }
                java.nio.file.Files.writeString(java.nio.file.Path.of(evidenceFile), JSON.writerWithDefaultPrettyPrinter().writeValueAsString(evidence));
            }
            System.out.println("PASS both Chrome editors via real HTTP and production scenes: 35 received model requests, selected suggestion, compacted business and language context, historical field/original validation, workspace denial and new chat");
        } finally { service.stop(); service.destroy(); }
    }
}
