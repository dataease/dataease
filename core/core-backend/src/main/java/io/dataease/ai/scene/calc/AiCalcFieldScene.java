package io.dataease.ai.scene.calc;

import io.dataease.ai.engine.conversation.AiConversationScene;
import io.dataease.ai.engine.runtime.AiToolProtocol;
import io.dataease.ai.support.AiErrors;
import io.dataease.ai.support.AiPromptResources;
import io.dataease.ai.tool.datasource.AiDatasourceTools;
import io.dataease.ai.tool.datasource.DatasetSqlSemanticTools;
import io.dataease.api.ai.conversation.AiConversationDtos.*;
import io.dataease.api.ai.runtime.AiPromptFingerprint;
import io.dataease.dataset.manage.DatasetDataManage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class AiCalcFieldScene implements AiConversationScene {
    private static final String INSTRUCTION = AiPromptResources.read("editor-calc-field.md");
    private static final String REVALIDATE = "revalidate_current_formula";
    private static final AiToolProtocol.Definition CURRENT_FORMULA = new AiToolProtocol.Definition(REVALIDATE,
            "Preview the unchanged current editor formula on explicit request, using this question's frozen draft.",
            Map.of(), AiToolProtocol.RetrySafety.READ_ONLY);
    private static final String PROPOSE = "propose_calc_field";
    private static final AiToolProtocol.Definition TOOL = new AiToolProtocol.Definition(PROPOSE,
            "Propose one detail or aggregate calculated field. Preview checks this expression in a private copy of the current dataset.",
            Map.of("expression", "Calculated-field expression using the exact [reference] provided for each field or parameter", "name", "Suggested field name",
                    "groupType", "d for dimension, q for measure", "deType", "0 text, 1 date, 2 integer, 3 decimal, 5 geography or 7 URL"),
            AiToolProtocol.RetrySafety.READ_ONLY);
    private record Draft(long target, String dataset, List<ResourceRevision> sources, Map<Long, CalcFieldAccess.Column> columns,
                         CalcFieldAccess.Grant grant, boolean preserveType, String currentExpression) implements SceneState { }

    private final io.dataease.ai.tool.sql.AiSqlStaticValidator sqlValidator = new io.dataease.ai.tool.sql.AiSqlStaticValidator();

    @jakarta.annotation.PreDestroy
    public void close() { sqlValidator.close(); }

    private final AiDatasourceTools metadata;
    private final DatasetSqlSemanticTools semanticTools;
    private final CalcFieldAccess access;
    private final DatasetDataManage preview;

    public AiCalcFieldScene(AiDatasourceTools metadata, CalcFieldAccess access, DatasetDataManage preview,
                            DatasetSqlSemanticTools semanticTools) {
        this.metadata = metadata;
        this.semanticTools = semanticTools;
        this.access = access;
        this.preview = preview;
    }

    @Override public String code() { return "dataset-calc-field"; }
    @Override public List<Resource> resources() { return List.of(); }
    @Override public Context resolve(String resourceId, String input) { throw AiErrors.error("invalid"); }
    @Override public List<AiToolProtocol.Definition> tools() { return List.of(TOOL, CURRENT_FORMULA); }
    @Override public boolean editorConversation() { return true; }

    @Override public Context resolve(Open editor) {
        var input = editor.calcField();
        if (input == null || input.dataset() == null || input.fieldId() == null || editor.preview() != null
                || editor.systemVariableNames() != null || editor.resourceId() == null) throw AiErrors.error("invalid");
        if (editor.isCross() == null || !Objects.equals(editor.isCross(), input.dataset().getIsCross()))
            throw AiErrors.error("unsupported");
        var resource = metadata.resolve(editor.resourceId());
        boolean isCross = editor.isCross();
        if (!isCross && !"mysql".equalsIgnoreCase(resource.type())) throw AiErrors.error("unsupported");
        var scope = AiDatasourceTools.Scope.editor(resource.id(), resource.revision(), isCross);
        var draft = CalcFieldCanvas.copy(CalcFieldCanvas.json(input.dataset()));
        var canvas = new CalcFieldCanvas(metadata, scope, sqlValidator);
        var saved = access.authorizedDataset(draft);
        canvas.rebuild(draft, saved);
        if (draft.getAllFields() == null || draft.getAllFields().size() > 256
                || draft.getAllFields().stream().anyMatch(Objects::isNull)) throw AiErrors.error("invalid");
        var grant = access.grant(draft, canvas.columns, input.fieldId(), saved);
        if (canvas.hasDraftSql && (!grant.hidden().isEmpty() || grant.rowRestricted())) throw AiErrors.error("calc_sql_manual");
        CalcFieldInputs.checkNodes(draft.getUnion().getFirst(), grant);
        var semantics = new CalcFieldSemantics(canvas, semanticTools);
        var fields = new CalcFieldInputs(draft, input.fieldId(), grant, canvas.columns, semantics);
        var tableMeanings = semantics.tables();
        // This is the author-selected editor text, including unfinished input. Only tool candidates execute.
        String basis = Objects.toString(editor.sql(), "");
        String instruction = INSTRUCTION + "\n" + AiToolProtocol.result(Map.of(
                "executionDialect", isCross ? "DataEase Calcite" : "MySQL", "fields", fields.projection,
                "tables", tableMeanings, "currentExpression", basis));
        String snapshot = CalcFieldCanvas.json(draft);
        // Bind every physical source revision. Discard the metadata caches after this open operation.
        var sources = canvas.sources.values().stream()
                .map(s -> new ResourceRevision(s.resourceId(), s.revision(), isCross)).toList();
        String revision = AiPromptFingerprint.of(new Prompt(snapshot + CalcFieldCanvas.json(fields.projection) + CalcFieldCanvas.json(tableMeanings)
                + CalcFieldCanvas.json(sources) + grant.savedVersion() + input.fieldId()));
        return new Context(resource.id(), resource.name(), revision, instruction, null, isCross, null,
                new Draft(input.fieldId(), snapshot, sources, Map.copyOf(canvas.columns), grant, Boolean.TRUE.equals(input.preserveType()),
                        Objects.toString(input.currentExpression(), basis)));
    }

    @Override public void authorize(Context context) {
        var scope = (Draft) context.sceneState();
        for (var source : scope.sources())
            metadata.authorize(new AiDatasourceTools.Scope(source.id(), source.revision(), source.isCross()));
        var grant = access.grant(CalcFieldCanvas.copy(scope.dataset()), scope.columns(), scope.target());
        if (!grant.equals(scope.grant())) throw AiErrors.error("scope_changed");
    }

    @Override public ToolExecution beginTurn(Context context) {
        var scope = (Draft) context.sceneState();
        var clarification = new java.util.concurrent.atomic.AtomicReference<String>();
        return (name, args, active, deadline) -> {
            boolean current = REVALIDATE.equals(name);
            if (!current && !PROPOSE.equals(name)) throw AiErrors.error("unsupported");
            active.run();
            authorize(context);
            if (clarification.get() != null) return clarification.get();
            var draft = CalcFieldCanvas.copy(scope.dataset());
            var fields = new CalcFieldInputs(draft, scope.target(), scope.grant(), scope.columns(), null);
            String expression = current ? scope.currentExpression() : args.get("expression");
            String fieldName = args.get("name");
            String group = args.get("groupType");
            String type = args.get("deType");
            if (!current && (fieldName == null || fieldName.isBlank() || fieldName.length() > 128 || !("q".equals(group) || "d".equals(group))
                    || type == null || !type.matches("[012357]"))) return invalid();
            String display;
            try {
                display = fields.candidate(expression, current ? 0 : Integer.parseInt(type), current || scope.preserveType());
            } catch (CalcFieldInputs.Ambiguous ambiguity) {
                String question = AiToolProtocol.result(Map.of("status", "CLARIFY_FIELD", "choices", ambiguity.options));
                clarification.set(question);
                return question;
            } catch (IllegalArgumentException invalid) { return invalid(); }
            if (!fields.includePreviewFields(draft))
                return verdict("PREVIEW_FAILED", "dataset_preview_failed", display);
            // Keep the diagnostic baseline independent of aliases/SQL expansion mutated by a failed preview.
            String bound = fields.target.getOriginName();
            String snapshot = CalcFieldCanvas.json(draft);
            Runnable guard = () -> {
                active.run();
                authorize(context);
                if (System.currentTimeMillis() >= deadline) throw AiErrors.error("timeout");
            };
            guard.run();
            String validation;
            try {
                validation = preview.previewCalcField(draft, scope.target(), deadline);
            } catch (Exception failure) {
                guard.run();
                checkPreviewAccess(failure);
                if (!isExpressionError(failure, context.isCross())) return verdict("PREVIEW_FAILED", "preview_unavailable", display);
                // Only attribute a Provider error to this candidate if the same authorized preview
                // succeeds with a neutral expression and all joins and siblings still present.
                var baseline = CalcFieldCanvas.copy(snapshot);
                baseline.getAllFields().stream().filter(f -> f.getId() == scope.target()).findFirst().orElseThrow().setOriginName("1");
                try {
                    preview.previewCalcField(baseline, scope.target(), deadline);
                } catch (Exception other) {
                    guard.run();
                    checkPreviewAccess(other);
                    return verdict("PREVIEW_FAILED", "dataset_preview_failed", display);
                }
                guard.run();
                return verdict("INVALID", "formula_execution_failed", display);
            }
            guard.run();
            if ("NO_PREVIEW_ROWS".equals(validation)) return verdict("PREVIEW_FAILED", "no_preview_rows", display);
            return AiToolProtocol.result(Map.of("status", validation, "code", "", "expression", display, "boundExpression", bound));
        };
    }

    private static void checkPreviewAccess(Exception error) {
        if (error instanceof AiErrors.Failure known
                && !Set.of("upstream_failed", "connection_failed", "provider_unavailable").contains(known.safeCode())) throw known;
        if (error instanceof io.dataease.exception.DEException de) {
            if (de.getCategory() == io.dataease.exception.ErrorCategory.FORBIDDEN
                    || de.getCategory() == io.dataease.exception.ErrorCategory.UNAUTHENTICATED) throw AiErrors.error("forbidden");
            if (de.getCategory() == io.dataease.exception.ErrorCategory.CONFLICT
                    || de.getCategory() == io.dataease.exception.ErrorCategory.NOT_FOUND) throw AiErrors.error("scope_changed");
        }
    }

    private static boolean isExpressionError(Exception failure, boolean isCross) {
        for (Throwable e = failure; e != null; e = e.getCause()) {
            if (isCross && e instanceof org.apache.calcite.sql.validate.SqlValidatorException) return true;
            if (e instanceof java.sql.SQLException sql) {
                String state = sql.getSQLState();
                // MySQL includes access denials in class 42. Accept only known expression diagnostics there.
                if (state != null && (state.startsWith("22") || state.startsWith("42")
                        && Set.of(1054, 1064, 1305, 1582).contains(sql.getErrorCode()))) return true;
            }
        }
        return false;
    }

    private static String verdict(String status, String reason, String expression) {
        return AiToolProtocol.result(Map.of("status", status, "code", reason, "expression", expression));
    }

    private static String invalid() {
        return AiToolProtocol.result(Map.of("status", "INVALID", "code", "invalid_formula",
                "message", "Check the field references, expression syntax and suggested properties, then submit a corrected candidate."));
    }

    @Override public String proposedContent(String tool, Map<String, String> arguments) {
        return PROPOSE.equals(tool) ? arguments.get("expression") : null;
    }

    @Override public String modelToolResult(String name, String result) {
        if (!PROPOSE.equals(name) && !REVALIDATE.equals(name)) return result;
        try {
            var feedback = CalcFieldCanvas.JSON.readTree(result);
            if ("CLARIFY_FIELD".equals(feedback.path("status").asText())) return result;
            return AiToolProtocol.result(Map.of("status", feedback.path("status").asText(), "code", feedback.path("code").asText()));
        } catch (java.io.IOException malformed) { throw AiErrors.error("response_invalid"); }
    }

    @Override public Map<String, Object> historicalEvidence(Context original, HistoricalStep step, HistoricalFields fields) {
        if (!(PROPOSE.equals(step.name()) || REVALIDATE.equals(step.name())) || !"SUCCEEDED".equals(step.status()) || step.result() == null) return Map.of();
        try {
            var feedback = CalcFieldCanvas.JSON.readTree(modelToolResult(step.name(), step.result()));
            var evidence = new LinkedHashMap<String, Object>();
            // The model supplied this expression. The normalized display and private binding belong to the editor.
            evidence.put("expression", REVALIDATE.equals(step.name()) ? ((Draft) original.sceneState()).currentExpression() : step.parameters().get("expression"));
            evidence.put("status", feedback.path("status").asText());
            if (feedback.hasNonNull("code") && !feedback.path("code").asText().isBlank())
                evidence.put("code", feedback.get("code").asText());
            return evidence;
        } catch (java.io.IOException malformed) { throw AiErrors.error("response_invalid"); }
    }

    @Override public String visibleToolResult(String name, String result) {
        try {
            var visible = (com.fasterxml.jackson.databind.node.ObjectNode) CalcFieldCanvas.JSON.readTree(result);
            visible.remove("boundExpression");
            return visible.toString();
        } catch (java.io.IOException invalid) { throw AiErrors.error("response_invalid"); }
    }

    @Override public boolean retainToolFailure(String name, String result) {
        if (!PROPOSE.equals(name)) return false;
        try {
            String status = CalcFieldCanvas.JSON.readTree(result).path("status").asText();
            return "INVALID".equals(status) || "PREVIEW_FAILED".equals(status);
        } catch (java.io.IOException malformed) { throw AiErrors.error("response_invalid"); }
    }

    @Override public Result result(Context context, String text, List<Tool> tools, String editorId, String draftVersion) {
        CalcFieldSuggestion candidate = null;
        for (var tool : tools) {
            if (!PROPOSE.equals(tool.name()) || !"SUCCEEDED".equals(tool.status())) continue;
            var args = tool.parameters();
            try {
                var feedback = CalcFieldCanvas.JSON.readTree(tool.result());
                String status = feedback.path("status").asText();
                if ("CLARIFY_FIELD".equals(status)) {
                    candidate = null;
                    continue;
                }
                String expression = feedback.path("expression").asText();
                if (expression.isEmpty()) {
                    if (canApply(status)) throw AiErrors.error("response_invalid");
                    expression = args.get("expression");
                }
                candidate = new CalcFieldSuggestion(expression, args.get("name"), args.get("groupType"),
                        args.get("deType").matches("[012357]") ? Integer.parseInt(args.get("deType")) : 0, status, feedback.path("code").asText(),
                        canApply(status) ? feedback.path("boundExpression").asText(null) : null);
            } catch (java.io.IOException malformed) { throw AiErrors.error("response_invalid"); }
        }
        return new Result(candidate == null ? "EXPLANATION" : "CALC_FIELD_SUGGESTION", text, null, context.resourceId(),
                context.resourceName(), context.revision(), editorId, draftVersion, List.of(), candidate);
    }

    @Override public boolean applicable(Result result) {
        return result.calcField() != null && canApply(result.calcField().validation());
    }

    static boolean canApply(String validation) {
        return "DETAIL_PASS".equals(validation) || "AGGREGATE_PLACEHOLDER".equals(validation);
    }
}
