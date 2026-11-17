package io.dataease.ai.scene.datasource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.ai.security.identity.AiAccess;
import io.dataease.ai.engine.task.AiBusinessTaskScene;
import io.dataease.ai.engine.task.AiTaskManage;
import io.dataease.ai.support.AiErrors;
import io.dataease.api.ds.DatasourceMetadataAiApi.*;
import io.dataease.api.ds.DatasourceRelationApi.*;
import io.dataease.api.ds.DatasourceSemanticApi.TableSemantic;
import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.api.permissions.auth.dto.BusiPerCheckDTO;
import io.dataease.constant.AuthEnum;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.datasource.manage.DatasourceRelationManage;
import io.dataease.datasource.manage.DatasourceSemanticManage;
import io.dataease.datasource.manage.DatasourceSemanticManage.SemanticSuggestion;
import io.dataease.datasource.manage.DatasourceSemanticManage.FieldSuggestion;
import io.dataease.datasource.server.DatasourceMetadataSource;
import io.dataease.system.manage.CorePermissionManage;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class AiDatasourceMetadataScenes {
    private static final int MAX_TABLES = 10;
    private final ObjectProvider<AiTaskManage> tasks;
    private final AiAccess access;
    private final ObjectProvider<InteractiveAuthApi> authorization;
    private final CorePermissionManage permissions;
    private final DatasourceMetadataSource physical;
    private final DatasourceRelationManage relations;
    private final DatasourceSemanticManage semantics;
    private final ObjectMapper json;

    public AiDatasourceMetadataScenes(ObjectProvider<AiTaskManage> tasks, AiAccess access,
                                      ObjectProvider<InteractiveAuthApi> authorization, CorePermissionManage permissions,
                                      DatasourceMetadataSource physical, DatasourceRelationManage relations,
                                      DatasourceSemanticManage semantics, ObjectMapper json) {
        this.tasks = tasks;
        this.access = access;
        this.authorization = authorization;
        this.permissions = permissions;
        this.physical = physical;
        this.relations = relations;
        this.semantics = semantics;
        this.json = json;
    }

    public Task submit(Long datasourceId, Kind kind, List<String> tables) {
        return view(tasks.getObject().submitBusiness(code(kind), datasourceId.toString(), tables));
    }

    public Task status(Long datasourceId, Kind kind, String taskId) {
        return view(tasks.getObject().businessStatus(code(kind), datasourceId.toString(), taskId));
    }

    @Bean
    AiBusinessTaskScene datasourceRelationTask() {
        return new MetadataScene(Kind.RELATION);
    }

    @Bean
    AiBusinessTaskScene datasourceSemanticTask() {
        return new MetadataScene(Kind.SEMANTIC);
    }

    private static String code(Kind kind) {
        return kind == Kind.RELATION ? "datasource-relations" : "datasource-semantics";
    }

    private record Summary(int written, int skipped, int failed) {}

    private Task view(AiTaskManage.BusinessStatus task) {
        if (task == null) return null;
        Summary summary;
        try {
            summary = task.summary() == null ? new Summary(0, 0, 0) : json.readValue(task.summary(), Summary.class);
        } catch (java.io.IOException invalid) {
            throw AiErrors.error("response_invalid");
        }
        Status status = switch (task.status()) {
            case "QUEUED" -> Status.QUEUED;
            case "RUNNING" -> Status.RUNNING;
            case "COMPLETED" -> summary.failed() == 0 ? Status.SUCCESS : Status.PARTIAL;
            default -> summary.written() > 0 || summary.skipped() > 0 ? Status.PARTIAL : Status.FAILED;
        };
        return new Task(task.id(), status, summary.written(), summary.skipped(), summary.failed());
    }

    private final class MetadataScene implements AiBusinessTaskScene {
        private final Kind kind;

        private MetadataScene(Kind kind) {
            this.kind = kind;
        }

        @Override
        public String code() {
            return AiDatasourceMetadataScenes.code(kind);
        }

        @Override
        public void authorizeResource(String resourceId) {
            Long id = Long.valueOf(resourceId);
            checkPermission(access.capture(), id);
            physical.datasource(id);
        }

        @Override
        public ResourceClaim resourceClaim(String resourceId) {
            return new ResourceClaim(resourceId, "*", code());
        }

        @Override
        public Context resolveTask(String resourceId, String ignored, List<String> tables) {
            authorizeResource(resourceId);
            if (tables == null || tables.isEmpty() || tables.size() > MAX_TABLES
                    || tables.stream().anyMatch(t -> t == null || t.isBlank() || t.length() > 512)) {
                throw AiErrors.error("invalid");
            }
            var selected = tables.stream().distinct().toList();
            if (kind == Kind.RELATION && selected.size() < 2) throw AiErrors.error("invalid");
            var source = physical.datasource(Long.valueOf(resourceId));
            return new Context(resourceId, source.getName(), version(source), "", selected);
        }

        @Override
        public void authorize(Context context) {
            authorizeResource(context.resourceId());
            if (!context.revision().equals(version(physical.datasource(Long.valueOf(context.resourceId()))))) {
                throw AiErrors.error("scope_changed");
            }
        }

        @Override
        public void execute(Context context, Execution execution) {
            Long id = Long.valueOf(context.resourceId());
            if (kind == Kind.RELATION) runRelations(id, context.selectedTables(), execution);
            else runSemantics(id, context.selectedTables(), execution);
        }
    }

    private record Column(String name, String type, String comment) {}
    private record TableMetadata(String name, String comment, List<Column> fields) {}
    private record RelationSuggestion(String name, String leftTable, String rightTable, List<Condition> conditions,
                                      Cardinality cardinality, JoinType joinType, String description) {}
    private record RelationSuggestions(List<RelationSuggestion> relations) {}
    private record FieldTarget(String fieldName, boolean businessName, boolean description, boolean aliases) {
        boolean hasMissingValues() { return businessName || description || aliases; }
    }

    private record SemanticInput(TableMetadata metadata, boolean description, boolean grain, List<FieldTarget> fields) {}

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private TableMetadata metadata(Long datasourceId, String name) {
        var source = physical.datasource(datasourceId);
        return metadata(source, DatasourceMetadataSource.listedTable(physical.tables(datasourceId, source), name));
    }

    private TableMetadata metadata(io.dataease.dao.auto.entity.CoreDatasource source,
                                   io.dataease.extensions.datasource.dto.DatasetTableDTO table) {
        return new TableMetadata(table.getTableName(), table.getName(),
                physical.fields(source, table.getTableName()).stream().filter(f -> f.getOriginName() != null)
                        .map(f -> new Column(f.getOriginName(), Objects.toString(f.getFieldType(), f.getType()), f.getName()))
                        .toList());
    }

    private Map<String, TableMetadata> relationMetadata(Long datasourceId, List<String> tables) {
        var source = physical.datasource(datasourceId);
        var available = physical.tables(datasourceId, source);
        Map<String, TableMetadata> result = new LinkedHashMap<>();
        for (String name : tables) {
            result.put(name, metadata(source, DatasourceMetadataSource.listedTable(available, name)));
        }
        return result;
    }

    private void runRelations(Long datasourceId, List<String> selectedTables, AiBusinessTaskScene.Execution execution) {
        var snapshot = relationMetadata(datasourceId, selectedTables);
        var answer = generate(execution, AiDatasourceMetadataPrompts.RELATIONS, snapshot.values(), RelationSuggestions.class);
        if (answer.relations() == null || answer.relations().size() > 100) throw AiErrors.error("invalid");
        execution.check();
        var current = relationMetadata(datasourceId, selectedTables);
        var candidates = answer.relations().stream().filter(candidate -> valid(candidate, snapshot, current))
                .map(r -> new Relation(null, r.name().strip(), r.leftTable(), r.rightTable(), r.conditions(),
                        r.cardinality(), r.joinType(), r.description(), false, true, List.of())).toList();
        execution.write(() -> {
            int added = relations.addSuggestions(datasourceId, candidates);
            return new Summary(added, answer.relations().size() - added, 0);
        });
    }

    private static boolean valid(RelationSuggestion r, Map<String, TableMetadata> snapshot,
                                 Map<String, TableMetadata> current) {
        if (r == null || r.name() == null || r.name().isBlank() || r.name().strip().length() > 255
                || r.description() != null && r.description().length() > 4000
                || r.cardinality() == null || r.joinType() == null || r.conditions() == null
                || r.conditions().isEmpty() || r.conditions().size() > 100
                || Objects.equals(r.leftTable(), r.rightTable())) return false;
        var left = current.get(r.leftTable());
        var right = current.get(r.rightTable());
        if (left == null || right == null || !left.equals(snapshot.get(r.leftTable()))
                || !right.equals(snapshot.get(r.rightTable()))) return false;
        var leftNames = names(left);
        var rightNames = names(right);
        return r.conditions().stream().allMatch(c -> c != null && leftNames.contains(c.leftField())
                && rightNames.contains(c.rightField())) && new HashSet<>(r.conditions()).size() == r.conditions().size();
    }

    private static Set<String> names(TableMetadata table) {
        return table.fields().stream().map(Column::name).collect(Collectors.toSet());
    }

    private void runSemantics(Long datasourceId, List<String> selectedTables, AiBusinessTaskScene.Execution execution) {
        Summary summary = new Summary(0, 0, 0);
        Map<String, TableSemantic> tables = semantics.list(datasourceId).tables().stream()
                .filter(t -> t.active() && t.available()).collect(Collectors.toMap(TableSemantic::tableName, t -> t));
        for (String name : selectedTables) {
            execution.check();
            Summary previous = summary;
            try {
                var table = tables.get(name);
                if (table == null) throw AiErrors.error("scope_changed");
                var snapshot = metadata(datasourceId, name);
                var fields = semantics.detail(datasourceId, table.id()).fields().stream()
                        .filter(field -> !field.confirmed())
                        .map(field -> new FieldTarget(field.fieldName(), blank(field.businessName()),
                                blank(field.description()), blank(field.aliases())))
                        .filter(FieldTarget::hasMissingValues).toList();
                boolean description = !table.confirmed() && blank(table.description());
                boolean grain = !table.confirmed() && blank(table.grain());
                if (!description && !grain && fields.isEmpty()) {
                    summary = execution.write(() -> new Summary(previous.written(), previous.skipped() + 1, previous.failed()));
                    continue;
                }
                var answer = generate(execution, AiDatasourceMetadataPrompts.SEMANTICS, new SemanticInput(snapshot, description, grain, fields),
                        SemanticSuggestion.class);
                var targets = fields.stream().collect(Collectors.toMap(FieldTarget::fieldName, target -> target));
                var scopedFields = answer.fields() == null ? List.<FieldSuggestion>of()
                        : answer.fields().stream().filter(field -> field != null && targets.containsKey(field.fieldName()))
                        .map(field -> {
                            var target = targets.get(field.fieldName());
                            return new FieldSuggestion(
                                    field.fieldName(), target.businessName() ? field.businessName() : null,
                                    target.description() ? field.description() : null, target.aliases() ? field.aliases() : null);
                        }).toList();
                var scoped = new SemanticSuggestion(description ? answer.description() : null,
                        grain ? answer.grain() : null, scopedFields);
                execution.check();
                var current = metadata(datasourceId, name);
                if (!snapshot.equals(current)) {
                    summary = execution.write(() -> new Summary(previous.written(), previous.skipped() + 1, previous.failed()));
                    continue;
                }
                summary = execution.write(() -> {
                    int written = semantics.applySuggestion(datasourceId, table, scoped, names(current));
                    return new Summary(previous.written() + written,
                            previous.skipped() + (written == 0 ? 1 : 0), previous.failed());
                });
            } catch (RuntimeException failure) {
                execution.check();
                summary = execution.write(() -> new Summary(previous.written(), previous.skipped(), previous.failed() + 1));
                if (summary.failed() == selectedTables.size()) throw failure;
            }
        }
    }

    private <T> T generate(AiBusinessTaskScene.Execution execution, String instructions, Object input, Class<T> type) {
        try {
            String data = json.writeValueAsString(input);
            if (data.length() > 64_000) throw AiErrors.error("invalid");
            var options = new OpenAiChatOptions();
            options.setResponseFormat(ResponseFormat.builder().type(ResponseFormat.Type.JSON_OBJECT).build());
            var prompt = new Prompt(List.of(new SystemMessage(instructions), new UserMessage(data)), options);
            String output = execution.call(prompt).strip();
            if (output.startsWith("```json") && output.endsWith("```")) {
                output = output.substring(7, output.length() - 3).strip();
            }
            T result = json.readerFor(type).with(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                    .readValue(output);
            if (result == null) throw AiErrors.error("invalid");
            return result;
        } catch (java.io.IOException invalid) {
            throw AiErrors.error("invalid");
        }
    }

    private String version(io.dataease.dao.auto.entity.CoreDatasource source) {
        return source.getType() + ":" + source.getUpdateTime();
    }

    private void checkPermission(AiAccess.Identity owner, Long id) {
        access.verify(owner, false);
        var request = new BusiPerCheckDTO(id, BusiResourceEnum.DATASOURCE, AuthEnum.MANAGE);
        if (owner.deployment().organizations()) {
            var auth = authorization.getIfAvailable();
            if (auth == null) throw AiErrors.error("forbidden");
            if (!physical.visible(id)) throw AiErrors.error("forbidden");
            auth.checkAuth(request);
        } else if (!permissions.checkAuth(request)) {
            throw AiErrors.error("forbidden");
        }
    }

}
