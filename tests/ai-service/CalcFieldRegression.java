package io.dataease.ai;

import com.fasterxml.jackson.databind.JsonNode;
import io.dataease.ai.scene.calc.AiCalcFieldScene;
import io.dataease.ai.scene.calc.CalcFieldAccess;
import io.dataease.ai.support.AiErrors;
import io.dataease.ai.security.identity.AiAccess;
import io.dataease.ai.security.identity.AiDeploymentMode;
import io.dataease.constant.AuthEnum;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.api.permissions.auth.dto.BusiPerCheckDTO;
import io.dataease.dataset.manage.DatasetGroupManage;
import io.dataease.dataset.manage.DatasetTableFieldManage;
import io.dataease.dataset.manage.PermissionManage;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import io.dataease.resource.RootFolderCreatePermissionManage;
import io.dataease.system.manage.CorePermissionManage;
import io.dataease.ai.tool.datasource.AiDatasourceTools;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.dataset.manage.DatasetDataManage;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static io.dataease.ai.ToolConversationRegression.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/** Controlled model + actual editor conversation. Replace only the external business adapters. */
final class CalcFieldRegression {
    final AtomicBoolean writable = new AtomicBoolean(true);
    final AtomicInteger previews = new AtomicInteger();
    private CountDownLatch previewStarted;
    private CountDownLatch previewRelease;
    private volatile String previewMode = "";
    private final String formula = "[Price] * 2";

    AiCalcFieldScene scene(AiDatasourceTools metadata) {
        var identity = new AiAccess(null, null) {
            @Override public Identity capture() {
                return new Identity(1L, null, null, new AiDeploymentMode.State(AiDeploymentMode.Mode.COMMUNITY, "fixture"));
            }
            @Override public void verify(Identity identity, boolean configuration) { }
        };
        var destinations = new RootFolderCreatePermissionManage() {
            @Override public void check(Long pid, BusiResourceEnum resource) {
                if (!writable.get() || !Objects.equals(pid, 0L) || resource != BusiResourceEnum.DATASET)
                    throw AiErrors.error("forbidden");
            }
        };
        var groups = new DatasetGroupManage(null) {
            @Override public List<BusiNodeVO> tree(BusiNodeRequest request) {
                var node = new BusiNodeVO();
                node.setLeaf(true); node.setId(42L);
                return List.of(node);
            }
            @Override public DatasetGroupInfoDTO getForCount(Long id) {
                var saved = new DatasetGroupInfoDTO();
                saved.setId(42L); saved.setNodeType("dataset"); saved.setLastUpdateTime(99L);
                saved.setAllFields(List.of()); saved.setUnion(List.of());
                return saved;
            }
        };
        var permissions = new CorePermissionManage() {
            @Override public boolean checkAuth(BusiPerCheckDTO request) {
                return writable.get() && Objects.equals(request.getId(), 42L)
                        && request.getAuthEnum() == AuthEnum.MANAGE && request.getBusiEnum() == BusiResourceEnum.DATASET;
            }
        };
        var fields = new DatasetTableFieldManage(null) {
            @Override public List<DatasetTableFieldDTO> selectByFieldIds(List<Long> ids) { return List.of(); }
        };
        var access = new CalcFieldAccess(identity, null, null, null, permissions, destinations, groups, fields, new PermissionManage());
        var preview = new DatasetDataManage() {
            @Override public String previewCalcField(DatasetGroupInfoDTO draft, long fieldId, long deadline) throws Exception {
                String value = draft.getAllFields().stream().filter(f -> f.getId() == fieldId).findFirst().orElseThrow().getOriginName();
                if (previewMode.startsWith("calc-unit-")) {
                    String expected = switch (previewMode) {
                        case "calc-unit-cents" -> "[102] / 100 * [params_9210000000000000003]";
                        case "calc-unit-yuan" -> "[102] * [params_9210000000000000003]";
                        case "calc-unit-converted" -> "[302] * [params_9210000000000000003]";
                        case "calc-unit-literal" -> "[303] * [params_9210000000000000003]";
                        default -> throw new AssertionError("Ambiguous proposal reached preview");
                    };
                    if (!value.equals(expected) || draft.getAllFields().size() != 4)
                        throw new AssertionError("Unexpected bound preview " + value);
                    return "DETAIL_PASS";
                }
                if (Set.of("calc-stop", "calc-revoke").contains(previewMode)) {
                    previewStarted.countDown();
                    previewRelease.await(5, TimeUnit.SECONDS);
                    throw new java.sql.SQLException("private-driver-value", "22012");
                }
                if (previewMode.equals("calc-join")) {
                    if (!draft.getIsCross() || !value.equals("[103] + [102]")
                            || draft.getUnion().getFirst().getChildrenDs().getFirst().getCurrentDs().getDatasourceId() != 12L)
                        throw new AssertionError("wrong second source or field binding");
                    return "DETAIL_PASS";
                }
                if (Set.of("calc-mysql", "calc-calcite").contains(previewMode)) {
                    boolean cross = previewMode.equals("calc-calcite");
                    String expected = cross ? "EXTRACT(MONTH FROM [102])" : "MONTH([102])";
                    if (draft.getIsCross() != cross || !expected.equals(value)) throw new AssertionError("preview dialect " + value);
                    return "DETAIL_PASS";
                }
                if ("calc-comment".equals(previewMode)) {
                    if (!"[102] - (-2)".equals(value)) throw new AssertionError("ambiguous preview expression");
                    return "DETAIL_PASS";
                }
                if ("calc-sibling".equals(previewMode)
                        && draft.getAllFields().stream().anyMatch(f -> Objects.equals(f.getId(), 302L)))
                    throw new java.sql.SQLException("private-driver-value", "22012");
                if ("calc-outage".equals(previewMode)) throw new java.sql.SQLException("private-driver-value", "08006");
                if ("calc-db-repair".equals(previewMode)) {
                    if (value.equals("ABS([102], 2)")) throw new java.sql.SQLException("private-driver-value", "42000", 1582);
                    if (value.equals("1")) return "DETAIL_PASS";
                }
                if (draft.getId() != null || fieldId != 301 || deadline <= System.currentTimeMillis()
                        || draft.getAllFields().size() != 2 || !"[102] * 2".equals(draft.getAllFields().getLast().getOriginName())
                        || draft.getAllFields().getLast().getDeType() != 2)
                    throw new AssertionError("detail preview must use bound field IDs in the unsaved MySQL draft");
                previews.incrementAndGet();
                return "DETAIL_PASS";
            }
        };
        return new AiCalcFieldScene(metadata, access, preview);
    }

    String respond(String mode, int step) throws Exception {
        String explanation = switch (mode) {
            case "calc-unit-ask" -> "Which Amount do you mean, the source column or the calculated field? What is its unit?";
            case "calc-unit-grain" -> "Do you want a row-level amount or a grouped total?";
            case "calc-unit-missing" -> "Which available field represents tax?";
            case "calc-unit-cents" -> "Divide cents by 100 to return yuan.";
            case "calc-unit-yuan" -> "The input is yuan; keep the same unit.";
            case "calc-unit-literal" -> "The separately named input is in yuan.";
            case "calc-unit-converted" -> "The calculated input is already in yuan; keep the same unit.";
            default -> "Confirmed detail projection";
        };
        if (mode.equals("calc-unit-grain") || mode.equals("calc-unit-missing"))
            return frame(Map.of("content", explanation), "stop");
        String candidate = switch (mode) {
            case "calc-unit-ask" -> "[Amount] * [Rate]";
            case "calc-unit-cents" -> "[Amount #102 #102] / 100 * [Rate]";
            case "calc-unit-yuan" -> "[Amount #102 #102] * [Rate]";
            case "calc-unit-converted" -> "[Amount #302] * [Rate]";
            case "calc-unit-literal" -> "[Amount #102 #303] * [Rate]";

            case "calc-join" -> "[Other] + [Price]";
            case "calc-mysql" -> step == 1 ? "EXTRACT(MONTH FROM [Order date])" : "MONTH([Order date])";
            case "calc-calcite" -> step == 1 ? "MONTH([Order date])" : "EXTRACT(MONTH FROM [Order date])";
            case "calc-invalid" -> "[Unknown] * 2";
            case "calc-exhaust" -> "[Missing" + step + "] + 1";
            case "calc-repair" -> step == 1 ? "[Missing] + 1" : formula;
            case "calc-db-repair" -> step == 1 ? "ABS([Price], 2)" : formula;
            case "calc-comment" -> "[Price]-- 2";
            default -> formula;
        };
        return step == 1 || "calc-invalid".equals(mode) || "calc-exhaust".equals(mode)
                || Set.of("calc-repair", "calc-db-repair", "calc-mysql", "calc-calcite").contains(mode) && step == 2
                ? call("formula-" + step, "propose_calc_field", JSON.writeValueAsString(Map.of("expression", candidate,
                        "name", "Double price", "groupType", "q", "deType", "3")))
                : frame(Map.of("content", explanation), "stop");
    }

    void verify(MockMvc mvc, AtomicReference<String> mode, List<JsonNode> requests, AtomicBoolean secondResourceAllowed) throws Exception {
        String opening = """
                {"scene":"dataset-calc-field","resourceId":"11","editorId":"calc-301","sql":"","isCross":false,
                 "calcField":{"fieldId":"301","preserveType":true,"dataset":{"pid":"0","isCross":false,
                  "union":[{"currentDs":{"id":"1","datasourceId":"11","type":"db","tableName":"AI_PROBE","info":"{\\"table\\":\\"AI_PROBE\\"}"},
                  "currentDsFields":[{"id":"102","datasetTableId":"1","datasourceId":"11","name":"Price","originName":"ID","checked":true,"deType":2,"extField":0}]}],
                  "allFields":[{"id":"102","datasetTableId":"1","datasourceId":"11","name":"Price","originName":"ID","deType":2,"extField":0,"checked":true,
                  "description":"private-context-value"},{"id":"301","name":"","extField":2,"deType":2,"originName":"","checked":true}]}}}
                """;
        verifyDialects(mvc, mode, requests, opening);
        requests.clear();
        verifyJoin(mvc, mode, opening, secondResourceAllowed);
        requests.clear();
        mode.set("calc-detail");
        var session = JSON.readTree(mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON).content(opening))
                .andReturn().getResponse().getContentAsString());
        var run = start(mvc, session, "Double the price");
        var result = terminal(mvc, run);
        var candidate = result.path("result").path("calcField");
        if (!"DETAIL_PASS".equals(candidate.path("validation").asText()) || !formula.equals(candidate.path("expression").asText())
                || !"Double price".equals(candidate.path("name").asText()) || candidate.path("deType").asInt() != 3
                || !result.path("result").path("sql").isNull() || previews.get() != 1 || requests.size() != 2
                || !requests.getFirst().toString().contains("Price") || requests.toString().contains("private-context-value")
                || requests.toString().contains("sample-secret")) throw new AssertionError("bounded detail projection: " + result);
        var application = JSON.writeValueAsBytes(Map.of("contextVersion",session.path("contextVersion").asText(),
                "draftVersion",session.path("draftVersion").asText()));
        var applied = mvc.perform(post(run + "/check-application").contentType(MediaType.APPLICATION_JSON).content(application))
                .andReturn().getResponse();
        if (!JSON.readTree(applied.getContentAsString()).path("calcField").equals(candidate)) throw new AssertionError("application projection");
        requests.clear(); mode.set("calc-invalid");
        var bad = terminal(mvc, start(mvc, session, "Use a missing field"));
        if (!"COMPLETED".equals(bad.path("status").asText()) || previews.get() != 1 || requests.size() != 2
                || !"INVALID".equals(bad.at("/result/calcField/validation").asText())
                || !"[Unknown] * 2".equals(bad.at("/result/calcField/expression").asText()))
            throw new AssertionError("unknown field must stop before the datasource preview");
        for (String scenario : List.of("calc-repair", "calc-exhaust", "calc-comment", "calc-outage", "calc-db-repair", "calc-sibling", "calc-unsafe-sibling")) {
            requests.clear();
            mode.set(scenario);
            previewMode = scenario;
            String data = opening;
            if (scenario.endsWith("sibling")) data = opening.replace("{\"id\":\"301\",\"name\":\"\"", """
                    {"id":"302","name":"Other","extField":2,"deType":2,"checked":true,"originName":"%s"},{"id":"301","name":""
                    """.strip().formatted(scenario.equals("calc-sibling") ? "1 / 0" : "unsupported()"));
            var isolated = JSON.readTree(mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON).content(data))
                    .andReturn().getResponse().getContentAsString());
            var path = start(mvc, isolated, "Calculate the field");
            var report = terminal(mvc, path);
            var field = report.at("/result/calcField");
            String expected = switch (scenario) {
                case "calc-exhaust" -> "INVALID";
                case "calc-outage", "calc-sibling", "calc-unsafe-sibling" -> "PREVIEW_FAILED";
                default -> "DETAIL_PASS";
            };
            int sends = switch (scenario) {
                case "calc-exhaust", "calc-db-repair", "calc-repair" -> 3;
                case "calc-comment" -> 2;
                default -> 1;
            };
            if (!expected.equals(field.path("validation").asText()) || requests.size() != sends
                    || report.toString().contains("private-driver-value") || requests.toString().contains("private-driver-value"))
                throw new AssertionError(scenario + " " + field + " sends=" + requests.size());
            if ("calc-comment".equals(scenario) && !"[Price] - (-2)".equals(field.path("expression").asText()))
                throw new AssertionError("application must return the validated arithmetic");
            if (!"DETAIL_PASS".equals(expected)) {
                if (field.path("reason").asText().isBlank()) throw new AssertionError("missing finite failure reason");
                if ("calc-exhaust".equals(scenario) && !field.path("expression").asText().equals("[Missing3] + 1"))
                    throw new AssertionError("must retain final failed candidate");
                try {
                    if (mvc.perform(post(path + "/check-application").contentType(MediaType.APPLICATION_JSON)
                            .content(JSON.writeValueAsBytes(Map.of("contextVersion", isolated.path("contextVersion").asText(),
                                    "draftVersion", isolated.path("draftVersion").asText()))))
                            .andReturn().getResponse().getStatus() < 400) throw new AssertionError("failed formula was applicable");
                } catch (jakarta.servlet.ServletException rejected) {
                    if (!(rejected.getCause() instanceof AiErrors.Failure)) throw rejected;
                }
            }
            mvc.perform(delete("/ai/conversations/" + isolated.path("sessionId").asText()));
        }
        for (String boundary : List.of("calc-stop", "calc-revoke")) {
            mode.set(boundary); requests.clear();
            previewStarted = new CountDownLatch(1); previewRelease = new CountDownLatch(1);
            previewMode = boundary;
            var isolated = JSON.readTree(mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON).content(opening))
                    .andReturn().getResponse().getContentAsString());
            String path = start(mvc, isolated, "Calculate the field");
            if (!previewStarted.await(5, TimeUnit.SECONDS)) throw new AssertionError("preview never started");
            try {
                if (boundary.equals("calc-stop")) {
                    var stopped = JSON.readTree(mvc.perform(post(path + "/stop")).andReturn().getResponse().getContentAsString());
                    if (!stopped.path("status").asText().equals("CANCELLED")) throw new AssertionError("preview still running");
                    previewRelease.countDown();
                    var cancelled = terminal(mvc, path);
                    if (!cancelled.path("result").isNull() || cancelled.toString().contains("SUCCEEDED"))
                        throw new AssertionError("cancelled validation returned a candidate");
                } else {
                    writable.set(false);
                    denied(mvc, path);
                }
            } finally {
                previewRelease.countDown(); writable.set(true);
                mvc.perform(delete("/ai/conversations/" + isolated.path("sessionId").asText()));
            }
            if (requests.size() != 1) throw new AssertionError("cancelled/revoked preview requested model correction");
        }
        previewMode = "";
        writable.set(false);
        try {
            if (mvc.perform(post(run + "/check-application").contentType(MediaType.APPLICATION_JSON).content(application))
                    .andReturn().getResponse().getStatus() < 400) throw new AssertionError("stale grant enabled application");
        } catch (jakarta.servlet.ServletException rejected) {
            if (!(rejected.getCause() instanceof AiErrors.Failure)) throw rejected;
        } finally {
            writable.set(true);
            requests.clear();
        }
        var saved = opening.replace("\"pid\":\"0\"", "\"id\":\"42\",\"pid\":\"0\"");
        var reopened = JSON.readTree(mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON).content(saved))
                .andReturn().getResponse().getContentAsByteArray());
        if (reopened.path("sessionId").asText().isBlank()) throw new AssertionError("authorized saved dataset must open");
        writable.set(false);
        try {
            expectDenied(mvc, saved);
        } finally { writable.set(true); }
        expectDenied(mvc, saved.replace("\"id\":\"42\"", "\"id\":\"43\""));
        expectDenied(mvc, opening.replace("\"pid\":\"0\"", "\"pid\":\"43\""));
        mvc.perform(delete("/ai/conversations/" + reopened.path("sessionId").asText()));
        // An unfinished author input is useful to the model even though it cannot run as a candidate.
        requests.clear();
        mode.set("calc-detail");
        String manual = "[Price] * (";
        var manualOpening = JSON.readValue(opening, com.fasterxml.jackson.databind.node.ObjectNode.class);
        manualOpening.put("sql", manual);
        var manualSession = JSON.readTree(mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsBytes(manualOpening))).andReturn().getResponse().getContentAsByteArray());
        terminal(mvc, start(mvc, manualSession, "Finish my formula"));
        if (!requests.getFirst().path("messages").get(0).path("content").asText().contains(manual))
            throw new AssertionError("manual formula basis missing in model request");
        mvc.perform(delete("/ai/conversations/" + manualSession.path("sessionId").asText()));
        verifyUnits(mvc, mode, requests, opening);
        System.out.println("PASS calculated-field HTTP conversation, bounded private preview, correction/exhaustion/duplicate, safe failure classification, canonical application, cancellation and revocation");
    }
    private void verifyUnits(MockMvc mvc, AtomicReference<String> mode, List<JsonNode> requests, String opening) throws Exception {
        var editor = (com.fasterxml.jackson.databind.node.ObjectNode) JSON.readTree(opening);
        var fields = (com.fasterxml.jackson.databind.node.ArrayNode) editor.at("/calcField/dataset/allFields");
        var raw = (com.fasterxml.jackson.databind.node.ObjectNode) fields.get(0);
        raw.put("name", "Amount").put("semanticConfirmed", true).put("semanticBusinessName", "Invoiced total")
                .put("semanticDescription", "Minor currency units before rounding");
        ((com.fasterxml.jackson.databind.node.ObjectNode) fields.get(1)).putArray("params").addObject()
                .put("id", "params_9210000000000000003").put("name", "Rate").put("value", "0.8");
        var converted = fields.addObject().put("id", "302").put("name", "Amount").put("extField", 2)
                .put("deType", 3).put("checked", true).put("originName", "[102] / [params_88]")
                .put("semanticConfirmed", false).put("semanticDescription", "unconfirmed-private-value");
        fields.addObject().put("id", "303").put("name", "Amount #102").put("extField", 2)
                .put("deType", 3).put("checked", true).put("originName", "[102] + 1");
        converted.putArray("params").addObject().put("id", "params_88").put("name", "Scale").put("value", "100");
        var session = JSON.readTree(mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsBytes(editor))).andReturn().getResponse().getContentAsByteArray());
        for (String unit : List.of("ask", "grain", "missing", "cents", "yuan", "converted", "literal")) {
            String scenario = "calc-unit-" + unit;
            requests.clear(); mode.set(scenario); previewMode = scenario;
            var question = switch (unit) {
                case "ask" -> "Calculate the amount";
                case "grain" -> "Calculate the total per order or across orders";
                case "missing" -> "Calculate tax using the missing tax field";
                case "cents" -> "Use the physical amount in cents per row and return yuan times Rate";
                case "literal" -> "Use the field literally named Amount #102";
                case "yuan" -> "For this turn use the physical amount which is already yuan times Rate";
                default -> "Use the calculated amount already converted to yuan times Rate";
            };
            var result = terminal(mvc, start(mvc, session, question)).path("result");
            String context = requests.getFirst().path("messages").get(0).path("content").asText();
            if (!context.contains("Invoiced total") || !context.contains("Minor currency units")
                    || !context.contains("Amount #102 #102") || !context.contains("Amount #302")
                    || !context.contains("Rate") || context.contains("unconfirmed-private-value") || context.contains("private-context-value"))
                throw new AssertionError("Missing draft references/confirmed meaning or leaked unconfirmed metadata");
            if (unit.equals("ask")) {
                if (!"EXPLANATION".equals(result.path("type").asText()) || !requests.toString().contains("CLARIFY_FIELD"))
                    throw new AssertionError("Expected clarification for the bare duplicate name: " + result);
            } else if (unit.equals("grain") || unit.equals("missing")) {
                if (!"EXPLANATION".equals(result.path("type").asText()) || !result.path("text").asText().endsWith("?")
                        || requests.size() != 1) throw new AssertionError("Expected an author question without a proposal");
            } else if (!result.path("text").asText().contains("yuan")
                    || result.path("calcField").path("boundExpression").asText().isBlank()
                    || !"DETAIL_PASS".equals(result.path("calcField").path("validation").asText()))
                throw new AssertionError("Unit-specific formula must pass: " + result);
        }
        previewMode = "";
        mvc.perform(delete("/ai/conversations/" + session.path("sessionId").asText()));
        System.out.println("PASS calculated-field clarification, confirmed draft semantics, parameterized references and units");
    }

    private void verifyJoin(MockMvc mvc, AtomicReference<String> mode, String opening, AtomicBoolean secondAllowed) throws Exception {
        previewMode = "calc-join";
        mode.set(previewMode);
        var tree = (com.fasterxml.jackson.databind.node.ObjectNode) JSON.readTree(opening);
        var dataset = (com.fasterxml.jackson.databind.node.ObjectNode) tree.at("/calcField/dataset");
        tree.put("isCross", true); dataset.put("isCross", true);
        var root = (com.fasterxml.jackson.databind.node.ObjectNode) dataset.at("/union/0");
        var child = root.deepCopy();
        ((com.fasterxml.jackson.databind.node.ObjectNode) child.get("currentDs")).put("datasourceId", "12").put("id", "2");
        var right = (com.fasterxml.jackson.databind.node.ObjectNode) child.at("/currentDsFields/0");
        right.put("datasourceId", "12").put("datasetTableId", "2").put("id", "103").put("originName", "SECOND_ID").put("name", "Other");
        var join = child.putObject("unionToParent");
        var key = join.put("unionType", "inner").putArray("unionFields").addObject();
        key.set("parentField", root.at("/currentDsFields/0")); key.set("currentField", right);
        root.putArray("childrenDs").add(child);
        ((com.fasterxml.jackson.databind.node.ArrayNode) dataset.get("allFields")).add(right);
        var session = JSON.readTree(mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON)
                .content(tree.toString())).andReturn().getResponse().getContentAsByteArray());
        var path = start(mvc, session, "Add the two joined values");
        if (!"DETAIL_PASS".equals(terminal(mvc, path).at("/result/calcField/validation").asText()))
            throw new AssertionError("authorized joined physical fields must preview");
        secondAllowed.set(false);
        try {
            expectDenied(mvc, tree.toString());
            try {
                var response = mvc.perform(post(path + "/check-application").contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsBytes(Map.of("contextVersion",session.path("contextVersion").asText(),
                                "draftVersion",session.path("draftVersion").asText())))).andReturn().getResponse();
                if (response.getStatus() < 400) throw new AssertionError("revoked secondary source allowed application");
            } catch (jakarta.servlet.ServletException denied) {
                if (!(denied.getCause() instanceof AiErrors.Failure failure) || !"forbidden".equals(failure.safeCode())) throw denied;
            }
        } finally { secondAllowed.set(true); }
        expectDenied(mvc, tree.toString().replace("\"isCross\":true", "\"isCross\":false"));
        expectDenied(mvc, tree.toString().replace("\"resourceId\":\"11\"", "\"resourceId\":\"12\""));
        mvc.perform(delete("/ai/conversations/" + session.path("sessionId").asText()));
        previewMode = "";
    }

    private void verifyDialects(MockMvc mvc, AtomicReference<String> mode, List<JsonNode> requests, String opening) throws Exception {
        for (boolean cross : List.of(false, true)) {
            previewMode = cross ? "calc-calcite" : "calc-mysql";
            mode.set(previewMode);
            requests.clear();
            var session = JSON.readTree(mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON)
                    .content(opening.replace("\"isCross\":false", "\"isCross\":" + cross)
                            .replace("\"Price\"", "\"Order date\"").replace("\"deType\":2,\"extField\":0", "\"deType\":1,\"extField\":0")))
                    .andReturn().getResponse().getContentAsByteArray());
            String path = start(mvc, session, "Return the month of the date");
            var candidate = terminal(mvc, path).at("/result/calcField");
            String expected = cross ? "EXTRACT(MONTH FROM [Order date])" : "MONTH([Order date])";
            if (!"DETAIL_PASS".equals(candidate.path("validation").asText()) || !expected.equals(candidate.path("expression").asText())
                    || requests.size() != 3) throw new AssertionError("dialect correction " + candidate + " sends=" + requests.size());
            for (var request : requests) {
                if (!request.path("messages").get(0).path("content").asText().contains(cross ? "DataEase Calcite" : "MySQL"))
                    throw new AssertionError("missing editor dialect after correction");
            }
            mvc.perform(delete("/ai/conversations/" + session.path("sessionId").asText()));
        }
        previewMode = "";
    }

    private static void expectDenied(MockMvc mvc, String opening) throws Exception {
        try {
            if (mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON).content(opening))
                    .andReturn().getResponse().getStatus() < 400) throw new AssertionError("unwritable dataset accepted");
        } catch (jakarta.servlet.ServletException rejected) {
            if (!(rejected.getCause() instanceof AiErrors.Failure)) throw rejected;
        }
    }
}
