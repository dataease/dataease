package io.dataease.ai;

import com.fasterxml.jackson.databind.JsonNode;
import io.dataease.ai.scene.calc.AiCalcFieldScene;
import io.dataease.ai.engine.conversation.AiConversationManage;
import io.dataease.ai.scene.calc.CalcFieldAccess;
import io.dataease.ai.tool.datasource.DatasetSqlSemanticTools;
import io.dataease.api.ds.DatasourceSemanticApi.*;
import io.dataease.datasource.manage.DatasourceSemanticManage;
import io.dataease.ai.support.AiErrors;
import io.dataease.ai.security.identity.AiAccess;
import io.dataease.ai.security.identity.AiDeploymentMode;
import io.dataease.constant.AuthEnum;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.api.permissions.auth.dto.BusiPerCheckDTO;
import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.api.permissions.dataset.api.ColumnPermissionsApi;
import io.dataease.api.permissions.dataset.api.RowPermissionsApi;
import io.dataease.extensions.view.dto.ColumnPermissionItem;
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
    private final CountDownLatch awaitingHistoryStop = new CountDownLatch(1);
    private final CountDownLatch releaseHistoryReply = new CountDownLatch(1);
    private volatile String previewMode = "";
    private CountDownLatch datasetRead;
    private CountDownLatch releaseRead;
    private DatasetGroupInfoDTO savedDataset;
    private final Map<String, Object> licensedBeans = new HashMap<>();
    private boolean licensed;
    private String columnPolicy = "";
    private final String formula = "[Price] * 2";

    AiCalcFieldScene scene(AiDatasourceTools metadata) {
        var identity = new AiAccess(null, null) {
            @Override public Identity capture() {
                return new Identity(1L, null, null, new AiDeploymentMode.State(licensed ? AiDeploymentMode.Mode.LICENSED : AiDeploymentMode.Mode.COMMUNITY, "fixture"));
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
                if (datasetRead != null) {
                    datasetRead.countDown();
                    try { if (!releaseRead.await(5, TimeUnit.SECONDS)) throw new AssertionError("blocked opening never released"); }
                    catch (InterruptedException interrupted) { throw new AssertionError(interrupted); }
                }
                if (savedDataset != null) return savedDataset;
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
        var plugins = new org.springframework.beans.factory.support.StaticListableBeanFactory(licensedBeans);
        var policies = new PermissionManage() {
            @Override public List<io.dataease.api.permissions.dataset.dto.DataSetRowPermissionsTreeDTO> getRowPermissionsTree(Long id, Long user) {
                if (!"row".equals(columnPolicy)) return List.of();
                var item = new io.dataease.extensions.view.dto.DatasetRowPermissionsTreeItem();
                item.setFieldId(102L);
                item.setField(savedDataset.getAllFields().getFirst());
                var tree = new io.dataease.extensions.view.dto.DatasetRowPermissionsTreeObj();
                tree.setItems(List.of(item));
                var rule = new io.dataease.api.permissions.dataset.dto.DataSetRowPermissionsTreeDTO();
                rule.setTree(tree);
                return List.of(rule);
            }
            @Override public List<DatasetTableFieldDTO> filterColumnPermissions(List<DatasetTableFieldDTO> fields,
                    Map<String, ColumnPermissionItem> masked, Long dataset, Long actor) {
                if ("hidden".equals(columnPolicy)) return fields.stream().filter(f -> !Objects.equals(f.getId(), 102L)).toList();
                if ("masked".equals(columnPolicy)) masked.put("saved_price", new ColumnPermissionItem());
                return fields;
            }
        };
        var access = new CalcFieldAccess(identity, plugins.getBeanProvider(InteractiveAuthApi.class),
                plugins.getBeanProvider(ColumnPermissionsApi.class), plugins.getBeanProvider(RowPermissionsApi.class),
                permissions, destinations, groups, fields, policies);
        var preview = new DatasetDataManage() {
            @Override public String previewCalcField(DatasetGroupInfoDTO draft, long fieldId, long deadline) throws Exception {
                String value = draft.getAllFields().stream().filter(f -> f.getId() == fieldId).findFirst().orElseThrow().getOriginName();
                if ("current-formula".equals(previewMode)) {
                    if (!"[102] + 77".equals(value)) throw new AssertionError("Provider received an earlier expression: " + value);
                    previews.incrementAndGet();
                    return "DETAIL_PASS";
                }
                if ("calc-column-policy".equals(previewMode)) {
                    if (!columnPolicy.isEmpty()) throw new AssertionError("Restricted field reached the provider");
                    return "DETAIL_PASS";
                }
                if (previewMode.equals("calc-draft-siblings")) {
                    if (!"[102] * 2".equals(value) || draft.getAllFields().size() != 6)
                        throw new AssertionError("The provider must preview all six draft fields");
                    return "DETAIL_PASS";
                }
                if ("calc-saved-sql".equals(previewMode)) {
                    if (!draft.getUnion().getFirst().getCurrentDs().getInfo().contains("sql") || !Set.of("[102] * 2", "[103] * 2").contains(value))
                        throw new AssertionError("Saved SQL preview lost the query or its bound field");
                    return "DETAIL_PASS";
                }
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
        var registry = new DatasourceSemanticManage(null, null, null, null) {
            @Override public Semantics list(Long source) {
                return new Semantics(source == 11L ? List.of(new TableSemantic(7L, "AI_PROBE", "source table", "one row per payment",
                        true, "", true, true, 1, 1)) : List.of());
            }
            @Override public TableDetails detail(Long source, Long tableId) {
                return new TableDetails(List.of(new FieldSemantic("AI_PROBE", "ID", "warehouse cents", "amount in minor units",
                        "", true, "BIGINT", "", List.of(new ValueReference("P", "source paid")))), 0);
            }
        };
        return new AiCalcFieldScene(metadata, access, preview, new DatasetSqlSemanticTools(metadata, registry));
    }

    String respond(String mode, int step) throws Exception {
        if ("calc-before-stop".equals(mode) && step == 2) {
            awaitingHistoryStop.countDown();
            if (!releaseHistoryReply.await(10, TimeUnit.SECONDS)) throw new AssertionError("unreleased formula reply");
        }
        if ("calc-refresh-current".equals(mode))
            return step == 1 ? call("recheck", "revalidate_current_formula", "{}") : frame(Map.of("content", "已重新校验"), "stop");
        if (Set.of("calc-history-latest", "calc-history-ambiguous").contains(mode)) {
            if (step == 1) return call("find-validation", "find_historical_step",
                    JSON.writeValueAsString(Map.of("action", "propose_calc_field", "subject", "*", "fields", "*", "selection", mode.endsWith("latest") ? "latest" : "any")));
            return frame(Map.of("content", mode.endsWith("ambiguous") ? "你指的是哪一次校验？" : "Historical validation"), "stop");
        }
        if ("calc-history-choices".equals(mode)) return frame(Map.of("content", "Which revenue convention?\n1. UNSELECTED_GROSS_MARKER\n2. Paid orders only"), "stop");
        if ("calc-history".equals(mode)) return frame(Map.of("content", "Historical validation"), "stop");
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

    void verify(AiConversationManage manage, MockMvc mvc, AtomicReference<String> mode, List<JsonNode> requests, AtomicBoolean secondResourceAllowed) throws Exception {
        String opening = """
                {"scene":"dataset-calc-field","resourceId":"11","editorId":"calc-301","sql":"","isCross":false,
                 "calcField":{"fieldId":"301","preserveType":true,"dataset":{"pid":"0","isCross":false,
                  "union":[{"currentDs":{"id":"1","datasourceId":"11","type":"db","tableName":"AI_PROBE","info":"{\\"table\\":\\"AI_PROBE\\"}"},
                  "currentDsFields":[{"id":"102","datasetTableId":"1","datasourceId":"11","name":"Price","originName":"ID","checked":true,"deType":2,"extField":0}]}],
                  "allFields":[{"id":"102","datasetTableId":"1","datasourceId":"11","name":"Price","originName":"ID","deType":2,"extField":0,"checked":true,
                  "description":"private-context-value"},{"id":"301","name":"","extField":2,"deType":2,"originName":"","checked":true}]}}}
                """;
        verifyColumnAccess(mvc, mode, requests, opening);
        verifyIndependentOpen(mvc, mode, opening);
        verifySiblings(mvc, mode, requests, opening);
        requests.clear();
        verifySavedSql(mvc, mode, requests, opening);
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
        String inherited = requests.getFirst().at("/messages/0/content").asText();
        if (!inherited.contains("warehouse cents") || !inherited.contains("source paid") || !inherited.contains("one row per payment"))
            throw new AssertionError("Missing confirmed datasource meanings for a physical field without dataset semantics");
        assertPrivateBindingAbsent(requests);
        var application = JSON.writeValueAsBytes(Map.of("contextVersion",session.path("contextVersion").asText(),
                "draftVersion",session.path("draftVersion").asText()));
        var applied = mvc.perform(post(run + "/check-application").contentType(MediaType.APPLICATION_JSON).content(application))
                .andReturn().getResponse();
        if (!JSON.readTree(applied.getContentAsString()).path("calcField").equals(candidate)) throw new AssertionError("application projection");
        requests.clear(); mode.set("calc-invalid");
        var bad = terminal(mvc, start(mvc, session, "Use a missing field"));
        assertPrivateBindingAbsent(requests);
        for (var request : requests) {
            String system = request.at("/messages/0/content").asText();
            if (!system.contains("回答语言") || !system.contains("计算字段编辑器") || system.contains("自定义 SQL 编辑器")
                    || !system.contains("本次对话") || !system.contains("直接追问"))
                throw new AssertionError("Calculated-field validation and its continuation need the editor language policy and formula instructions");
        }
        if (!"COMPLETED".equals(bad.path("status").asText()) || previews.get() != 1 || requests.size() != 2
                || !"INVALID".equals(bad.at("/result/calcField/validation").asText())
                || !"[Unknown] * 2".equals(bad.at("/result/calcField/expression").asText()))
            throw new AssertionError("unknown field must stop before the datasource preview");
        for (String scenario : List.of("calc-repair", "calc-exhaust", "calc-comment", "calc-outage", "calc-db-repair", "calc-sibling", "calc-unsafe-sibling")) {
            requests.clear();
            mode.set(scenario);
            previewMode = scenario;
            String data = opening;
            if ("calc-exhaust".equals(scenario)) data = data.replace("\"sql\":\"\"", "\"sql\":\"[Price] + 123\"");
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
            if ("calc-repair".equals(scenario)) {
                if (!requests.get(1).toString().contains("invalid_formula") || !requests.get(1).toString().contains("[Missing] + 1")
                        || report.path("attemptNumber").asInt() != 1)
                    throw new AssertionError("automatic correction must use the rejected formula in this answer");
                var validations = report.path("events").findParents("callId").stream()
                        .filter(tool -> "SUCCEEDED".equals(tool.path("status").asText())).toList();
                if (validations.size() != 2 || validations.get(0).path("callId").equals(validations.get(1).path("callId"))
                        || !"INVALID".equals(JSON.readTree(validations.getFirst().path("result").asText()).path("status").asText())
                        || !"DETAIL_PASS".equals(JSON.readTree(validations.getLast().path("result").asText()).path("status").asText())
                        || validations.toString().contains("boundExpression"))
                    throw new AssertionError("Both actual formula validations must be visible without private bindings");
            }
            if ("calc-exhaust".equals(scenario)) {
                if (!report.path("retryEligible").asBoolean()) throw new AssertionError("exhausted validation should allow manual retry");
                requests.clear(); mode.set("calc-detail");
                var retried = JSON.readTree(mvc.perform(post(path + "/retry").contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsBytes(Map.of("expectedAttemptId", report.path("attemptId").asText(),
                                "retryKey", "retry-exhausted-formula")))).andReturn().getResponse().getContentAsByteArray());
                var next = terminal(mvc, "/ai/conversations/" + isolated.path("sessionId").asText() + "/executions/" + retried.path("executionId").asText());
                var messages = requests.getFirst().path("messages");
                String userInput = messages.get(messages.size() - 1).path("content").asText();
                if (!report.path("taskId").equals(next.path("taskId")) || !report.path("attemptId").equals(next.path("sourceAttemptId"))
                        || !messages.get(0).path("content").asText().contains("[Price] + 123")
                        || !userInput.contains("Calculate the field") || !userInput.contains("[Missing3] + 1")
                        || !userInput.contains("invalid_formula") || !userInput.contains("propose_calc_field")
                        || !"DETAIL_PASS".equals(next.at("/result/calcField/validation").asText()))
                    throw new AssertionError("manual retry must retain question, rejected candidate, validation reason and attempt lineage");
                assertPrivateBindingAbsent(requests);
            }
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
        var service = ConversationSocketRegression.editorServer(manage);
        try {
            service.start();
            verifyHistoricalValidation(mvc, mode, requests, opening, "http://127.0.0.1:" + service.getPort() + "/de2api/ai/conversations");
        } finally {
            service.stop();
            service.destroy();
        }
        System.out.println("PASS calculated-field HTTP conversation, bounded private preview, correction/exhaustion/duplicate, safe failure classification, canonical application, cancellation and revocation");
    }
    private void verifyHistoricalValidation(MockMvc mvc, AtomicReference<String> mode, List<JsonNode> requests, String opening, String base) throws Exception {
        var http = new ConversationSocketRegression.EditorHttp();
        var editor = JSON.readValue(opening, com.fasterxml.jackson.databind.node.ObjectNode.class);
        editor.put("editorId", "history-calc");
        var original = http.ok("POST", base, editor);
        requests.clear();
        mode.set("calc-detail");
        var first = http.ask(base, original, "Double the price");
        if (!"DETAIL_PASS".equals(first.at("/result/calcField/validation").asText())) throw new AssertionError(first);
        int originalPreviews = previews.get();
        mode.set("calc-history-choices");
        terminal(mvc, start(mvc, original, "Which revenue convention?"));
        mode.set("calc-history");
        terminal(mvc, start(mvc, original, "2"));
        terminal(mvc, start(mvc, original, "確認：排除退款；接下來都用英文。"));
        for (int i = 0; i < 6; i++) terminal(mvc, start(mvc, original, "Continue example " + i));
        editor.put("sql", "[Price] + 77");
        editor.put("previousSessionId", original.path("sessionId").asText());
        var edited = http.ok("POST", base, editor);
        requests.clear();
        mode.set("calc-history");
        var explained = http.ask(base, edited, "第一次回答的第 1 步校验的是哪个公式？");
        if (requests.size() != 1 || previews.get() != originalPreviews || !"EXPLANATION".equals(explained.at("/result/type").asText()))
            throw new AssertionError("Historical explanation must not validate or recommend the edited formula");
        var messages = requests.getFirst().path("messages");
        String outbound = messages.toString();
        if (messages.size() > 10 || outbound.contains("UNSELECTED_GROSS_MARKER") || !outbound.contains("Paid orders only")
                || !outbound.contains("排除退款") || !outbound.contains("接下來都用英文"))
            throw new AssertionError("Long dialogue must compact early turns, preserve confirmed/selected terms and omit discarded options");
        var evidence = JSON.readTree(messages.get(messages.size() - 1).path("content").asText()).at("/historicalStep/evidence");
        if (!formula.equals(evidence.path("expression").asText()) || !"DETAIL_PASS".equals(evidence.path("status").asText())
                || !messages.get(0).path("content").asText().contains("[Price] + 77"))
            throw new AssertionError("The prior validation must identify its submitted expression, separately from the edited basis: " + evidence);
        for (var message : messages) {
            if (message.has("tool_calls") || "tool".equals(message.path("role").asText())
                    || message.path("content").asText().contains("private-context-value")
                    || message.path("content").asText().contains("boundExpression") || message.path("content").asText().contains("[102] * 2"))
                throw new AssertionError("A historical explanation replayed tool data or private formula bindings");
        }
        mode.set("calc-history-latest");
        requests.clear();
        var lastValidation = http.ask(base, edited, "上一次校验的公式是什么？");
        if (!"COMPLETED".equals(lastValidation.path("status").asText()) || requests.size() != 2 || previews.get() != originalPreviews)
            throw new AssertionError("Looking up the last validation must return its evidence without another preview: " + lastValidation);
        var lookupMessage = requests.getLast().path("messages").get(requests.getLast().path("messages").size() - 1);
        var found = JSON.readTree(lookupMessage.path("content").asText());
        if (!"tool".equals(lookupMessage.path("role").asText()) || !formula.equals(found.at("/evidence/expression").asText())
                || !"DETAIL_PASS".equals(found.at("/evidence/status").asText()) || found.path("answerNumber").asInt() != 1)
            throw new AssertionError("Last validation must skip intervening explanations and identify the exact proposal: " + found);
        mode.set("retry-truncated");
        requests.clear();
        String interrupted = start(mvc, edited, "第一次回答的第 1 步校验结果");
        var failure = terminal(mvc, interrupted);
        var frozenMessages = requests.getFirst().path("messages");
        var frozenStep = JSON.readTree(frozenMessages.get(frozenMessages.size() - 1).path("content").asText()).path("historicalStep");
        requests.clear();
        mode.set("calc-history");
        var retry = JSON.readTree(mvc.perform(post(interrupted + "/retry").contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsBytes(Map.of("retryKey", "retry-history-question", "expectedAttemptId", failure.get("attemptId").asText()))))
                .andReturn().getResponse().getContentAsByteArray());
        terminal(mvc, "/ai/conversations/" + edited.get("sessionId").asText() + "/executions/" + retry.get("executionId").asText());
        var retryMessages = requests.getFirst().path("messages");
        var retryInput = JSON.readTree(retryMessages.get(retryMessages.size() - 1).path("content").asText());
        if (!frozenStep.path("evidence").has("expression") || !frozenStep.equals(retryInput.path("historicalStep"))
                || !retryInput.path("previousAttemptDiagnostics").hasNonNull("reason") || previews.get() != originalPreviews)
            throw new AssertionError("Retrying a historical question must preserve its original evidence alongside the failure diagnostic");
        mode.set("calc-refresh-current");
        requests.clear();
        previewMode = "current-formula";
        var refreshed = http.ask(base, edited, "重新校验当前公式");
        if (requests.size() != 2 || previews.get() != originalPreviews + 1 || !"COMPLETED".equals(refreshed.path("status").asText())
                || !refreshed.toString().contains("DETAIL_PASS") || !refreshed.toString().contains("[Price] + 77"))
            throw new AssertionError("Fresh validation must preview the current frozen formula once: " + refreshed);
        previewMode = "";
        originalPreviews = previews.get();
        mode.set("calc-history-ambiguous");
        requests.clear();
        var clarified = http.ask(base, edited, "之前校验的那个公式是哪一版？");
        var matches = requests.getLast().path("messages");
        var versions = JSON.readTree(matches.get(matches.size()-1).path("content").asText());
        if (!"COMPLETED".equals(clarified.path("status").asText()) || !"AMBIGUOUS".equals(versions.path("lookup").asText())
                || versions.path("candidates").size() != 2 || !versions.toString().contains("[Price] + 77")
                || !versions.toString().contains(formula) || versions.toString().contains("[102]") || previews.get() != originalPreviews)
            throw new AssertionError("Several prior validations require one question identifying the two visible versions: " + versions);
        mode.set("calc-history");
        String id = edited.path("sessionId").asText();
        requests.clear();
        writable.set(false);
        try {
            var denied = http.request("POST", base + "/" + id + "/executions", Map.of("contextVersion",
                    edited.path("contextVersion").asText(), "text", "上一次校验的公式"), "writer", null);
            if (denied.statusCode() != 403) throw new AssertionError("Revoked historical formula: " + denied);
            if (!requests.isEmpty() || previews.get() != originalPreviews)
                throw new AssertionError("Revoked history reached the model or preview");
        } finally { writable.set(true); }
        http.ok("DELETE", base + "/" + id, null);

        editor.remove("previousSessionId");
        var fresh = http.ok("POST", base, editor);
        requests.clear();
        http.ask(base, fresh, "第一次回答的第 1 步");
        var freshMessages = requests.getFirst().path("messages");
        var lookup = JSON.readTree(freshMessages.get(freshMessages.size() - 1).path("content").asText()).path("historicalStep");
        if (!"MISSING".equals(lookup.path("lookup").asText()) || lookup.has("evidence"))
            throw new AssertionError("New conversation exposed an earlier formula validation");
        http.ok("DELETE", base + "/" + fresh.path("sessionId").asText(), null);
        verifyStoppedFormula(mvc, mode, requests, opening);
    }

    private void verifyStoppedFormula(MockMvc mvc, AtomicReference<String> mode, List<JsonNode> requests, String opening) throws Exception {
        var session = JSON.readTree(mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON)
                .content(opening)).andReturn().getResponse().getContentAsByteArray());
        requests.clear();
        mode.set("calc-before-stop");
        var path = start(mvc, session, "Validate the price");
        JsonNode stopped;
        try {
            if (!awaitingHistoryStop.await(5, TimeUnit.SECONDS)) throw new AssertionError("No continuation after validation");
            stopped = JSON.readTree(mvc.perform(post(path + "/stop")).andReturn().getResponse().getContentAsByteArray());
            if (!"CANCELLED".equals(stopped.path("status").asText())) throw new AssertionError(stopped);
        } finally { releaseHistoryReply.countDown(); }

        mode.set("calc-invalid");
        requests.clear();
        var retry = JSON.readTree(mvc.perform(post(path + "/retry").contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsBytes(Map.of("retryKey", "history-retry", "expectedAttemptId", stopped.get("attemptId").asText()))))
                .andReturn().getResponse().getContentAsByteArray());
        var rerun = terminal(mvc, "/ai/conversations/" + session.get("sessionId").asText() + "/executions/" + retry.get("executionId").asText());
        if (!rerun.path("events").toString().contains("Check the field references"))
            throw new AssertionError("The editor should receive the detailed validation message");
        var count = previews.get();
        mode.set("calc-history");
        for (var entry : List.of(Map.entry(1, "DETAIL_PASS"), Map.entry(2, "INVALID"))) {
            requests.clear();
            terminal(mvc, start(mvc, session, "第 " + entry.getKey() + " 次回答的第 1 步原公式结果"));
            var messages = requests.getFirst().path("messages");
            var step = JSON.readTree(messages.get(messages.size() - 1).path("content").asText()).path("historicalStep");
            if (!entry.getValue().equals(step.at("/evidence/status").asText()) || previews.get() != count
                    || requests.toString().contains("Check the field references"))
                throw new AssertionError("Stopped and retried answers must retain separate model-visible validation facts: " + step);
        }
        mvc.perform(delete("/ai/conversations/" + session.get("sessionId").asText()));
        System.out.println("PASS historical formula A/B, stopped answer and retry, no new preview, editor-only detail, revoked access and new conversation");
    }

    private void assertPrivateBindingAbsent(List<JsonNode> requests) {
        for (var request : requests) for (var message : request.path("messages")) {
            if (!"tool".equals(message.path("role").asText())) continue;
            String content = message.path("content").asText();
            if (content.contains("expression") || content.contains("boundExpression") || content.contains("[102]"))
                throw new AssertionError("Model received private formula result");
        }
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
        var table = (com.fasterxml.jackson.databind.node.ObjectNode) editor.at("/calcField/dataset/union/0/currentDs");
        table.put("semanticConfirmed", true).put("semanticGrain", "one row per invoice");
        raw.put("semanticAliases", "posted amount")
                .put("semanticValueReferences", "[{\"value\":\"A\",\"meaning\":\"settled locally\"}]");
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
            if (!context.contains("one row per invoice") || !context.contains("posted amount") || !context.contains("settled locally")
                    || context.contains("warehouse cents") || context.contains("one row per payment"))
                throw new AssertionError("Confirmed dataset field and table semantics take precedence");
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
        mode.set("calc-unit-ask");
        for (boolean localField : List.of(true, false)) {
            raw.put("semanticConfirmed", localField);
            table.put("semanticConfirmed", !localField);
            requests.clear();
            var mixed = JSON.readTree(mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON)
                    .content(JSON.writeValueAsBytes(editor))).andReturn().getResponse().getContentAsByteArray());
            terminal(mvc, start(mvc, mixed, "Calculate the amount"));
            var prompt = requests.getFirst().at("/messages/0/content").asText();
            if (prompt.contains("settled locally") != localField || prompt.contains("source paid") == localField
                    || prompt.contains("one row per invoice") == localField || prompt.contains("one row per payment") != localField)
                throw new AssertionError("Table and field semantic confirmations must resolve independently");
            mvc.perform(delete("/ai/conversations/" + mixed.path("sessionId").asText()));
        }
        System.out.println("PASS calculated-field clarification, dataset semantic priority, independent source fallback, parameters and units");
    }

    private void verifyColumnAccess(MockMvc mvc, AtomicReference<String> mode, List<JsonNode> requests, String opening) throws Exception {
        var tree = (com.fasterxml.jackson.databind.node.ObjectNode) JSON.readTree(opening);
        var dataset = (com.fasterxml.jackson.databind.node.ObjectNode) tree.at("/calcField/dataset");
        dataset.put("id", "42");
        savedDataset = JSON.treeToValue(dataset, DatasetGroupInfoDTO.class);
        savedDataset.setNodeType("dataset"); savedDataset.setLastUpdateTime(99L);
        savedDataset.getAllFields().getFirst().setDataeaseName("saved_price");
        licensed = true;
        for (var api : List.of(InteractiveAuthApi.class, ColumnPermissionsApi.class, RowPermissionsApi.class))
            licensedBeans.put(api.getName(), java.lang.reflect.Proxy.newProxyInstance(api.getClassLoader(), new Class[]{api},
                    (proxy, method, args) -> null));
        previewMode = "calc-column-policy";
        mode.set(previewMode);
        try {
            for (var api : List.of(ColumnPermissionsApi.class, RowPermissionsApi.class)) {
                var removed = licensedBeans.remove(api.getName());
                expectDenied(mvc, tree.toString());
                licensedBeans.put(api.getName(), removed);
            }
            for (String restriction : List.of("hidden", "masked")) {
                requests.clear();
                var session = JSON.readTree(mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON)
                        .content(tree.toString())).andReturn().getResponse().getContentAsByteArray());
                String path = start(mvc, session, "Double the price");
                if (!"DETAIL_PASS".equals(terminal(mvc, path).at("/result/calcField/validation").asText())) throw new AssertionError("Authorized price");
                columnPolicy = restriction;
                try {
                    var response = mvc.perform(post(path + "/check-application").contentType(MediaType.APPLICATION_JSON).content(
                            JSON.writeValueAsBytes(Map.of("contextVersion", session.path("contextVersion").asText(),
                                    "draftVersion", session.path("draftVersion").asText())))).andReturn().getResponse();
                    if (response.getStatus() < 400) throw new AssertionError("Column revocation allowed application");
                } catch (jakarta.servlet.ServletException revoked) {
                    if (!(revoked.getCause() instanceof AiErrors.Failure failure) || !"scope_changed".equals(failure.safeCode())) throw revoked;
                }
                requests.clear();
                session = JSON.readTree(mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON)
                        .content(tree.toString())).andReturn().getResponse().getContentAsByteArray());
                terminal(mvc, start(mvc, session, "Calculate a value"));
                String prompt = requests.getFirst().at("/messages/0/content").asText();
                if (prompt.contains("Price") || prompt.contains("warehouse cents") || prompt.contains("source paid"))
                    throw new AssertionError("Restricted column appeared in the model field list");
                mvc.perform(delete("/ai/conversations/" + session.path("sessionId").asText()));
                columnPolicy = "";
            }
        } finally {
            licensed = false; licensedBeans.clear(); savedDataset = null; columnPolicy = ""; previewMode = "";
        }
    }

    private void verifyIndependentOpen(MockMvc mvc, AtomicReference<String> mode, String opening) throws Exception {
        mode.set("calc-open-concurrency");
        var executor = java.util.concurrent.Executors.newFixedThreadPool(2);
        datasetRead = new CountDownLatch(1);
        releaseRead = new CountDownLatch(1);
        try {
            var blocked = executor.submit(() -> openAs(mvc, 1L, opening.replace("\"pid\":\"0\"", "\"id\":\"42\",\"pid\":\"0\"")));
            if (!datasetRead.await(5, TimeUnit.SECONDS)) throw new AssertionError("saved dataset resolution did not enter");
            var unrelated = executor.submit(() -> openAs(mvc, 2L, "{\"scene\":\"dataset-sql\",\"editorId\":\"another-editor\",\"sql\":\"\"}"));
            if (unrelated.get(2, TimeUnit.SECONDS).path("sessionId").asText().isBlank())
                throw new AssertionError("Unrelated editor did not open");
            releaseRead.countDown();
            if (blocked.get(5, TimeUnit.SECONDS).path("sessionId").asText().isBlank()) throw new AssertionError("Saved editor failed");
        } finally {
            releaseRead.countDown();
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
            datasetRead = null;
        }
    }

    private JsonNode openAs(MockMvc mvc, long actor, String opening) throws Exception {
        io.dataease.permission.util.V3UserUtil.setUid(actor);
        try {
            var session = JSON.readTree(mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON)
                    .content(opening)).andReturn().getResponse().getContentAsByteArray());
            mvc.perform(delete("/ai/conversations/" + session.path("sessionId").asText()));
            return session;
        } finally { io.dataease.permission.util.V3UserUtil.clear(); }
    }

    private void verifySiblings(MockMvc mvc, AtomicReference<String> mode, List<JsonNode> requests, String opening) throws Exception {
        var tree = (com.fasterxml.jackson.databind.node.ObjectNode) JSON.readTree(opening);
        var fields = (com.fasterxml.jackson.databind.node.ArrayNode) tree.at("/calcField/dataset/allFields");
        var group = fields.addObject().put("id", "401").put("name", "Price band").put("extField", 3)
                .put("originName", "102").put("deType", 0).put("otherGroup", "Other");
        group.putArray("groupList").addObject().put("name", "Small").put("min", "0").put("max", "100")
                .put("minTerm", "le").put("maxTerm", "lt");
        var rate = fields.addObject().put("id", "402").put("name", "Commission").put("extField", 2).put("deType", 3)
                .put("originName", "[102] * [params_901]");
        rate.putArray("params").addObject().put("id", "params_901").put("name", "Rate").put("value", "0.05");
        fields.add(rate.deepCopy().put("id", "403").put("name", "Commission copy"));
        fields.addObject().put("id", "404").put("name", "First digit").put("extField", 2).put("deType", 0)
                .put("originName", "SUBSTRING([102], 1, 1)");
        requests.clear(); mode.set("calc-draft-siblings"); previewMode = mode.get();
        try {
            var session = JSON.readTree(mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON)
                    .content(tree.toString())).andReturn().getResponse().getContentAsByteArray());
            if (!"DETAIL_PASS".equals(terminal(mvc, start(mvc, session, "Double Price")).at("/result/calcField/validation").asText())
                    || requests.getFirst().toString().contains("Price band") || requests.getFirst().toString().contains("params_901"))
                throw new AssertionError("An ordinary group, copied parameter and text calculation must coexist in preview");
            mvc.perform(delete("/ai/conversations/" + session.path("sessionId").asText()));
        } finally { previewMode = ""; }
    }

    private void verifySavedSql(MockMvc mvc, AtomicReference<String> mode, List<JsonNode> requests, String opening) throws Exception {
        var tree = (com.fasterxml.jackson.databind.node.ObjectNode) JSON.readTree(opening);
        var dataset = (com.fasterxml.jackson.databind.node.ObjectNode) tree.at("/calcField/dataset");
        dataset.put("id", "42");
        var table = (com.fasterxml.jackson.databind.node.ObjectNode) dataset.at("/union/0/currentDs");
        String query = Base64.getEncoder().encodeToString("SELECT ID FROM AI_PROBE".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        table.put("type", "sql").put("tableName", "AI_PROBE").put("info", JSON.writeValueAsString(Map.of("sql", query, "table", "AI_PROBE")));
        var outputColumn = (com.fasterxml.jackson.databind.node.ObjectNode) dataset.at("/union/0/currentDsFields/0");
        outputColumn.putNull("datasourceId");
        savedDataset = JSON.treeToValue(dataset, DatasetGroupInfoDTO.class);
        savedDataset.setNodeType("dataset"); savedDataset.setLastUpdateTime(99L);
        requests.clear();
        mode.set("calc-saved-sql"); previewMode = mode.get();
        try {
            var session = JSON.readTree(mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON)
                    .content(tree.toString())).andReturn().getResponse().getContentAsByteArray());
            if (!"DETAIL_PASS".equals(terminal(mvc, start(mvc, session, "Double the saved query's price")).at("/result/calcField/validation").asText()))
                throw new AssertionError("Saved read-only SQL columns must preview");
            var prompt = requests.getFirst().toString();
            if (prompt.contains("warehouse cents") || prompt.contains("one row per payment"))
                throw new AssertionError("A SQL node named after a physical table must not inherit its datasource semantics");
            mvc.perform(delete("/ai/conversations/" + session.path("sessionId").asText()));
            outputColumn.put("datasourceId", "12");
            expectDenied(mvc, tree.toString());
            outputColumn.put("datasourceId", "11");
            table.put("info", JSON.writeValueAsString(Map.of("sql", Base64.getEncoder().encodeToString(
                    "SELECT AI_PROBE.ID AS ID FROM AI_PROBE".getBytes()), "table", "AI_PROBE")));
            for (String policy : List.of("hidden", "row")) {
                columnPolicy = policy;
                try {
                    mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON).content(tree.toString()));
                    throw new AssertionError("Changed SQL and retained field IDs need manual validation under " + policy);
                } catch (jakarta.servlet.ServletException expected) {
                    if (!(expected.getCause() instanceof AiErrors.Failure failure) || !"calc_sql_manual".equals(failure.safeCode())) throw expected;
                } finally { columnPolicy = ""; }
            }
            String projected = Base64.getEncoder().encodeToString("SELECT ID AS AMOUNT FROM AI_PROBE".getBytes());
            table.put("info", JSON.writeValueAsString(Map.of("sql", projected, "table", "AI_PROBE")));
            ((com.fasterxml.jackson.databind.node.ObjectNode) dataset.at("/union/0/currentDsFields/0")).put("originName", "AMOUNT").put("id", "103");
            ((com.fasterxml.jackson.databind.node.ObjectNode) dataset.at("/allFields/0")).put("originName", "AMOUNT").put("id", "103");
            for (boolean saved : List.of(true, false)) {
                if (!saved) dataset.remove("id");
                requests.clear();
                var changed = JSON.readTree(mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON)
                        .content(tree.toString())).andReturn().getResponse().getContentAsByteArray());
                var field = terminal(mvc, start(mvc, changed, "Double the projected amount")).at("/result/calcField");
                if (!"DETAIL_PASS".equals(field.path("validation").asText()) || !"[103] * 2".equals(field.path("boundExpression").asText()))
                    throw new AssertionError("Current simple SELECT projection did not bind");
                mvc.perform(delete("/ai/conversations/" + changed.path("sessionId").asText()));
            }
            table.put("info", JSON.writeValueAsString(Map.of("sql", Base64.getEncoder().encodeToString("SELECT 2 AS ID".getBytes()), "table", "AI_PROBE")));
            expectDenied(mvc, tree.toString());
        } finally { savedDataset = null; previewMode = ""; }
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
        var key = join.put("unionType", "full").putArray("unionFields").addObject();
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
