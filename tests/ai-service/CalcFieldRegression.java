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
import static io.dataease.ai.ToolConversationRegression.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/** Controlled model + actual editor conversation. Replace only the external business adapters. */
final class CalcFieldRegression {
    final AtomicBoolean writable = new AtomicBoolean(true);
    final AtomicInteger previews = new AtomicInteger();
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
            @Override public String previewCalcField(DatasetGroupInfoDTO draft, long fieldId, long deadline) {
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
        String candidate = "calc-invalid".equals(mode) ? "[Unknown] * 2" : formula;
        return step == 1 || "calc-invalid".equals(mode)
                ? call("formula-" + step, "propose_calc_field", JSON.writeValueAsString(Map.of("expression", candidate,
                        "name", "Double price", "groupType", "q", "deType", "3")))
                : frame(Map.of("content", "Confirmed detail projection"), "stop");
    }

    void verify(MockMvc mvc, AtomicReference<String> mode, List<JsonNode> requests) throws Exception {
        String opening = """
                {"scene":"dataset-calc-field","resourceId":"11","editorId":"calc-301","sql":"","isCross":false,
                 "calcField":{"fieldId":"301","preserveType":true,"dataset":{"pid":"0","isCross":false,
                  "union":[{"currentDs":{"id":"1","datasourceId":"11","type":"db","tableName":"AI_PROBE","info":"{\\"table\\":\\"AI_PROBE\\"}"},
                  "currentDsFields":[{"id":"102","datasetTableId":"1","datasourceId":"11","name":"Price","originName":"ID","checked":true,"deType":2,"extField":0}]}],
                  "allFields":[{"id":"102","datasetTableId":"1","datasourceId":"11","name":"Price","originName":"ID","deType":2,"extField":0,"checked":true,
                  "description":"private-context-value"},{"id":"301","name":"","extField":2,"deType":2,"originName":"","checked":true}]}}}
                """;
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
        if (!"FAILED".equals(bad.path("status").asText()) || previews.get() != 1)
            throw new AssertionError("unknown field must stop before the datasource preview");
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
        System.out.println("PASS calculated-field HTTP conversation, bounded preview, unknown reference, application and revocation");
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
