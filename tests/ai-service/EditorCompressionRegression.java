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
        boolean lookupReply = "tool".equals(last.path("role").asText()) && "history-step".equals(last.path("tool_call_id").asText());
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
            String notes = envelope.path("earlierDialogue").toString();
            if (!List.of("已确认退款订单不计入收入", "已确认净收入以元计", "units are cents", "exclude archived orders")
                    .stream().allMatch(notes::contains))
                return frame(Map.of("content", "与一次性语言同句的业务口径丢失"), "stop");
            if (List.of("这次用英文", "用日文", "French", "German", "For this turn")
                    .stream().anyMatch(notes::contains))
                return frame(Map.of("content", "一次性回答语言仍在请求中"), "stop");
            if (messages.size() > (lookupReply ? 10 : 8) || !envelope.path("earlierDialogue").isArray()
                    || !envelope.path("earlierDialogue").toString().contains("exclude chargebacks")
                    || !envelope.path("earlierDialogue").toString().contains("Paid orders only")
                    || !envelope.path("earlierDialogue").toString().contains("接下来都用英文")
                    || !envelope.path("earlierDialogue").toString().contains("用繁體中文回答")
                    || messages.toString().contains("UNSELECTED_GROSS_MARKER")
                    || messages.toString().contains("old-schema") || messages.toString().contains("old-validation")
                    || messages.toString().contains("boundExpression") || envelope.has("historicalStep")
                    || !lookupReply && messages.toString().contains("tool_call_id"))
                return frame(Map.of("content", "历史未压缩或口径丢失"), "stop");
            var selected = envelope.path("selectedSuggestion");
            var basis = envelope.path("generationBasis");
            if (selected.path("number").asInt() != 1 || !(calc ? FORMULA : SQL).equals(selected.path("text").asText()))
                reply = "缺少作者选定的建议";
            else if (question.startsWith("第一次回答")) {
                if (!lookupReply) return lookupStep("1.1", calc ? "*" : "ID");
                var evidence = JSON.readTree(last.path("content").asText()).path("evidence");
                boolean found = calc ? FORMULA.equals(evidence.path("expression").asText()) && "DETAIL_PASS".equals(evidence.path("status").asText())
                        : "ID".equals(evidence.at("/fields/0/name").asText()) && evidence.path("fields").size() == 1;
                reply = found && "DRAFT".equals(basis.path("kind").asText()) && system.contains(calc ? "[Price] + 77" : "SELECT 77")
                        ? "原步驟正確，沿用已確認的口徑" : "缺少原步骤或新草稿";
            } else {
                reply = "SUGGESTION".equals(basis.path("kind").asText()) && basis.path("number").asInt() == 1
                        && !basis.path("matchesDraft").asBoolean(true) && system.contains(calc ? FORMULA : SQL)
                        ? "本輪依據為建議一，草稿另有修改" : "本轮依据不清楚";
            }
        } else if (question.equals("重新校验当前公式")) {
            if (!"SUGGESTION".equals(envelope.at("/generationBasis/kind").asText())
                    || envelope.at("/generationBasis/number").asInt() != 1 || !system.contains(FORMULA)
                    || messages.toString().contains("[Price] + 77")) {
                reply = "生成依据错误或额外发送了草稿";
            } else if (!"tool".equals(last.path("role").asText())) {
                return call("draft-validation", "revalidate_current_formula", "{}");
            } else {
                reply = "no_preview_rows".equals(JSON.readTree(last.path("content").asText()).path("code").asText())
                        ? "当前草稿预览无数据" : "校验了其它公式";
            }
        } else if (question.equals("解释刚才手动预览的问题")) {
            if (!envelope.has("previousAttemptDiagnostics")) return frame(Map.of("content", "未完成"), "length");
            reply = "按哪个期间？\n1. 按上月的预览统计\n2. 按本年统计";
        } else if (question.equals("按上月的预览统计")) {
            reply = "MANUAL_PREVIEW".equals(envelope.at("/generationBasis/kind").asText())
                    && "MANUAL_PREVIEW".equals(envelope.at("/currentTarget/basis").asText())
                    && system.contains("UNKNOWN_COLUMN") && system.contains("SELECT unknown_amount FROM AI_PROBE")
                    ? "按原失败预览分析" : "缺少原预览状态";
        } else if (question.equals("解释当前 SQL 草稿")) {
            reply = "DRAFT".equals(envelope.at("/generationBasis/kind").asText())
                    && !envelope.path("currentTarget").has("basis") && system.contains("SELECT 77 AS n")
                    ? "按当前草稿分析" : "缺少当前 SQL";
        } else if (question.equals("仅沿用已确认口径")) {
            reply = "NONE".equals(envelope.at("/generationBasis/kind").asText()) && !envelope.has("selectedSuggestion")
                    && envelope.path("earlierDialogue").toString().contains("exclude chargebacks")
                    ? "只沿用已確認口徑" : "空依据带入了旧建议";
        } else if (question.equals("换个话题，解释采购")) {
            String past = envelope.path("earlierDialogue").toString();
            reply = past.contains("接下来都用英文") && past.contains("用繁體中文回答") && !past.contains("这次用英文")
                    && system.contains("跨话题沿用") ? "Purchase definition" : "Missing conversation preference";
        } else if (question.equals("新会话的步骤 1？")) {
            if (!lookupReply) return lookupStep("last.1", "*");
            reply = !envelope.has("earlierDialogue") && !envelope.has("selectedSuggestion")
                    && "MISSING".equals(JSON.readTree(last.path("content").asText()).path("lookup").asText()) ? "新对话无此步骤" : "新对话带入旧依据";
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
            if (calls != 45) throw new AssertionError("Expected 41 model requests plus four history continuations: " + calls);
            var evidenceFile = System.getenv("AI_EDITOR_COMPRESSION_EVIDENCE");
            if (evidenceFile != null) {
                var evidence = new ArrayList<Object>();
                for (var call : requests.subList(first, requests.size())) {
                    var messages = call.path("messages");
                    var last = messages.get(messages.size() - 1);
                    String text = last.path("content").asText();
                    if ("tool".equals(last.path("role").asText()) && "history-step".equals(last.path("tool_call_id").asText())) {
                        evidence.add(Map.of("roles", messages.findValuesAsText("role"), "historyResult", JSON.readTree(text)));
                        continue;
                    }
                    if (!"user".equals(last.path("role").asText())) continue;
                    var envelope = text.startsWith("{") ? JSON.readTree(text) : JSON.getNodeFactory().textNode(text);
                    String question = envelope.path("userMessage").asText(text);
                    if (question.startsWith("第一次回答") || question.equals("新会话的步骤 1？") || question.equals("现在呢？")) {
                        evidence.add(Map.of("roles", messages.findValuesAsText("role"),
                                "scene", messages.get(0).path("content").asText().contains("计算字段编辑器") ? "dataset-calc-field" : "dataset-sql",
                                "user", envelope));
                    }
                }
                java.nio.file.Files.writeString(java.nio.file.Path.of(evidenceFile), JSON.writerWithDefaultPrettyPrinter().writeValueAsString(evidence));
            }
            System.out.println("PASS both Chrome editors via real HTTP and production scenes: 45 received model requests, selected suggestion, compacted business and language context, historical field/original validation, current draft revalidation, retried manual preview, workspace denial and new chat");
        } finally { service.stop(); service.destroy(); }
    }
}
