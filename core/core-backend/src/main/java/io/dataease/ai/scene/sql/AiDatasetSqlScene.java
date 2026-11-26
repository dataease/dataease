package io.dataease.ai.scene.sql;

import io.dataease.ai.engine.runtime.AiToolProtocol;
import io.dataease.ai.tool.AiTool;
import io.dataease.ai.support.AiErrors;
import io.dataease.ai.support.AiPromptResources;
import io.dataease.ai.tool.datasource.DatasetSqlSemanticTools;
import io.dataease.ai.tool.datasource.DatasetSqlTableSearch;
import io.dataease.ai.tool.datasource.AiDatasourceTools;
import io.dataease.api.ai.conversation.AiConversationDtos;
import io.dataease.utils.JsonUtil;
import io.dataease.datasource.manage.DatasourceRelationManage;
import io.dataease.extensions.datasource.utils.SqlUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/** The dataset editor delivers a draft. Run and Save remain explicit editor operations. */
@Component
public class AiDatasetSqlScene extends AbstractSqlConversationScene {
    private static final String INSTRUCTION = AiPromptResources.read("editor-dataset-sql.md");
    private final DatasetSqlGuide guide;

    public AiDatasetSqlScene(AiDatasourceTools metadata, DatasetSqlGuide guide, DatasetSqlSemanticTools semantics, DatasourceRelationManage relations) {
        super(metadata, List.of(semantics.search(), DatasetSqlTableSearch.tool(metadata), semantics.fields(), DatasetSqlRelations.tool(metadata, relations), guide.tool(metadata), proposal(metadata)));
        this.guide = guide;
    }

    @Override protected AiDatasourceTools.Scope scope(Context context) {
        return AiDatasourceTools.Scope.editor(context.resourceId(), context.revision(), context.isCross());
    }

    @Override public String code() { return "dataset-sql"; }
    @Override public AiToolProtocol.StepLimit toolStepLimit() { return AiToolProtocol.StepLimit.DATASET_SQL; }
    @Override public boolean editorConversation() { return true; }
    @Override public void authorizeCopyTarget(String target) {
        if (!target.matches("[0-9]{1,20}")) throw AiErrors.error("invalid");
        var source = metadata.resolve(target);
        metadata.authorize(AiDatasourceTools.Scope.editor(target, source.revision(), false));
    }
    @Override protected String proposalStatus() { return "NOT_RUN"; }

    @Override public Context resolve(AiConversationDtos.Open editor) {
        var context = resolve(java.util.Objects.toString(editor.resourceId(), ""), java.util.Objects.toString(editor.sql(), ""), Boolean.TRUE.equals(editor.isCross()));
        var names = editor.systemVariableNames() == null ? List.of() : editor.systemVariableNames();
        var preview = editor.preview();
        ResourceRevision currentEditor = null;
        if (preview != null && preview.currentResourceId() != null) {
            if (!preview.currentResourceId().matches("[0-9]{1,20}") || preview.currentIsCross() == null) throw AiErrors.error("invalid");
            var current = metadata.resolve(preview.currentResourceId());
            currentEditor = new ResourceRevision(current.id(), current.revision(), preview.currentIsCross());
        }
        if (preview != null) validatePreview(editor, preview);
        var data = new java.util.LinkedHashMap<String, Object>();
        data.put("systemVariableNames", names);
        if (preview != null) data.put("manualPreview", preview);
        return new Context(context.resourceId(), context.resourceName(), context.revision(), context.instruction() + "\n" + JsonUtil.toJSONString(data),
                context.selectedTables(), Boolean.TRUE.equals(editor.isCross()), currentEditor);
    }

    @Override public void authorize(Context context) {
        super.authorize(context);
        var current = context.editorResource();
        if (current != null && (!current.id().equals(context.resourceId()) || current.isCross() != context.isCross()))
            super.authorize(new Context(current.id(), "", current.revision(), "", null, current.isCross()));
    }

    private static void validatePreview(AiConversationDtos.Open editor, AiConversationDtos.SqlPreview preview) {
        if (editor.resourceId() == null || editor.resourceId().isBlank() || !editor.resourceId().equals(preview.resourceId())
                || preview.isCross() == null || preview.isCross() != Boolean.TRUE.equals(editor.isCross())
                || preview.outcome() == null || editor.sql() == null || editor.sql().isBlank()
                || preview.revision() == null || preview.currentRevision() == null
                || preview.revision() < 0 || preview.currentRevision() < preview.revision()) throw AiErrors.error("invalid");
    }

    @Override protected String unboundInstruction(String sql) {
        return INSTRUCTION + "\n" + JsonUtil.toJSONString(Map.of("selection", "NONE", "sqlBasis", sql));
    }

    @Override protected String instruction(AiDatasourceTools.Datasource source, String sql, boolean isCross) {
        String connector = guide.displayName(source.type());
        String dialectType = guide.executionType(source.type(), isCross);
        String dialect = dialectType.equalsIgnoreCase(source.type()) ? connector : guide.displayName(dialectType);
        return INSTRUCTION + "\n" + JsonUtil.toJSONString(Map.of("sourceConnector", connector, "executionDialect", dialect, "sqlBasis", sql));
    }

    @Override public AiConversationDtos.Result result(Context context, String text, List<AiConversationDtos.Tool> tools,
                                                       String editorId, String draftVersion) {
        var result = super.result(context, text, tools, editorId, draftVersion);
        if (result.sql() == null) return result;
        return new AiConversationDtos.Result(result.type(), text, result.sql(), result.resourceId(), result.resourceName(),
                result.resourceVersion(), editorId, draftVersion, DatasetSqlEvidence.forProposal(tools, result.sql()));
    }

    @Override public String proposedContent(String tool, Map<String, String> arguments) {
        return "propose_sql".equals(tool) ? arguments.get("sql") : null;
    }

    private static AiTool<AiDatasourceTools.Scope> proposal(AiDatasourceTools metadata) {
        var definition = new AiToolProtocol.Definition("propose_sql",
                "Offer one complete read-only query as an unrun editor draft. No execution, preview, or save.",
                Map.of("sql", "Complete query to offer for explicit application"), AiToolProtocol.RetrySafety.READ_ONLY);
        return new AiTool<>(definition, (scope, arguments, active, deadline) -> {
            metadata.authorize(scope);
            return AiToolProtocol.result(SqlUtil.isSingleReadQuery(arguments.get("sql"))
                    ? DatasetSqlEvidence.lineage(arguments.get("sql"))
                    : Map.of("status", "INVALID", "message", "Propose one complete read-only SELECT or WITH query."));
        });
    }
}
