package io.dataease.ai.scene.calc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.ai.support.AiErrors;
import io.dataease.ai.tool.sql.AiSqlStaticValidator;
import io.dataease.extensions.datasource.utils.SqlUtil;
import java.nio.charset.StandardCharsets;
import io.dataease.ai.tool.datasource.AiDatasourceTools;
import io.dataease.api.dataset.union.*;
import io.dataease.extensions.datasource.dto.*;
import java.util.*;

/** Rebuild executable table/column/join nodes from authorized physical metadata. */
final class CalcFieldCanvas {
    static final ObjectMapper JSON = new ObjectMapper();
    final Map<Long, CalcFieldAccess.Column> columns = new HashMap<>();
    final Map<Long, DatasetTableFieldDTO> physical = new HashMap<>();
    final Map<Long, DatasetTableDTO> tables = new LinkedHashMap<>();
    final Map<Long, AiDatasourceTools.TableFields> physicalTables = new HashMap<>();
    private final AiSqlStaticValidator sqlValidator;
    private final AiDatasourceTools metadata;
    boolean hasDraftSql;
    private final AiDatasourceTools.Scope scope;
    final Map<String, AiDatasourceTools.Scope> sources = new LinkedHashMap<>();

    CalcFieldCanvas(AiDatasourceTools metadata, AiDatasourceTools.Scope scope, AiSqlStaticValidator sqlValidator) {
        this.metadata = metadata;
        this.sqlValidator = sqlValidator;
        this.scope = scope;
        sources.put(scope.resourceId(), scope);
    }

    private final Map<Long, UnionDTO> savedSql = new HashMap<>();

    private void indexSaved(UnionDTO node) {
        if ("sql".equalsIgnoreCase(node.getCurrentDs().getType())) savedSql.put(node.getCurrentDs().getId(), node);
        if (node.getChildrenDs() != null) node.getChildrenDs().forEach(this::indexSaved);
    }

    void rebuild(DatasetGroupInfoDTO draft, DatasetGroupInfoDTO saved) {
        if (saved != null) saved.getUnion().forEach(this::indexSaved);
        if (draft.getUnion() == null || draft.getUnion().size() != 1) throw AiErrors.error("unsupported");
        node(draft.getUnion().getFirst(), null, 0);
        draft.setSortFields(null);
    }

    private void node(UnionDTO node, Map<Long, DatasetTableFieldDTO> parent, int depth) {
        if (node == null || depth > 8 || tables.size() >= 16) throw AiErrors.error("input_limit");
        var table = node.getCurrentDs();
        if (table == null) throw AiErrors.error("invalid");
        boolean sql = "sql".equalsIgnoreCase(table.getType());
        if (!sql && !"db".equalsIgnoreCase(table.getType())) throw AiErrors.error("unsupported");
        String id = String.valueOf(table.getDatasourceId());
        if ((parent == null || !scope.isCross()) && !scope.resourceId().equals(id)) throw AiErrors.error("scope_changed");
        var source = sources.computeIfAbsent(id, key -> {
            var resource = metadata.resolve(key);
            return AiDatasourceTools.Scope.editor(resource.id(), resource.revision(), true);
        });
        String name = table.getTableName();
        Set<String> allowed = new HashSet<>();
        Map<Long,String> savedColumns = new HashMap<>();
        if (sql) {
            var stored = savedSql.get(table.getId());
            try {
                var info = JSON.readTree(table.getInfo());
                if (info == null || !info.path("sql").isTextual() || !info.path("table").isTextual()) throw AiErrors.error("calc_sql_manual");
                String query = new String(Base64.getDecoder().decode(info.path("sql").asText()), StandardCharsets.UTF_8);
                if (!SqlUtil.isSingleReadQuery(query)) throw AiErrors.error("calc_sql_manual");
                var original = stored == null ? null : stored.getCurrentDs();
                if (original != null && !Objects.equals(original.getDatasourceId(), table.getDatasourceId())) throw AiErrors.error("scope_changed");
                if (original != null && info.equals(JSON.readTree(original.getInfo()))
                        && Objects.equals(table.getSqlVariableDetails(), original.getSqlVariableDetails())) {
                    table.setInfo(original.getInfo());
                    table.setSqlVariableDetails(original.getSqlVariableDetails());
                    name = original.getTableName();
                    table.setTableName(name);
                    for (var field : stored.getCurrentDsFields()) savedColumns.put(field.getId(), field.getOriginName());
                } else {
                    if (table.getSqlVariableDetails() != null && !table.getSqlVariableDetails().isBlank()
                            && !"[]".equals(table.getSqlVariableDetails().trim())) throw AiErrors.error("calc_sql_manual");
                    var projection = sqlValidator.columnProjection(query, physicalTable -> metadata.fields(source, physicalTable, true)
                            .fields().stream().map(AiDatasourceTools.Field::name).toList(), System.currentTimeMillis() + 10_000);
                    if (projection == null) throw AiErrors.error("calc_sql_manual");
                    hasDraftSql = true;
                    allowed.addAll(projection.outputs());
                    table.setSqlVariableDetails(null);
                    table.setInfo(JSON.writeValueAsString(Map.of("table", info.path("table").asText(), "sql",
                            Base64.getEncoder().encodeToString(projection.sql().getBytes(StandardCharsets.UTF_8)))));
                }
            } catch (java.io.IOException | IllegalArgumentException invalid) { throw AiErrors.error("calc_sql_manual"); }
        } else {
            if (!identifier(name)) throw AiErrors.error("invalid");
            try {
                if (!name.equals(JSON.readTree(table.getInfo()).path("table").asText())) throw AiErrors.error("invalid");
                table.setInfo(JSON.writeValueAsString(Map.of("table", name)));
            } catch (java.io.IOException failure) { throw AiErrors.error("invalid"); }
            table.setSqlVariableDetails(null);
            var structure = metadata.fields(source, name, true);
            physicalTables.put(table.getId(), structure);
            for (var field : structure.fields()) allowed.add(field.name());
        }
        if (table.getId() == null || name == null || tables.putIfAbsent(table.getId(), table) != null) throw AiErrors.error("invalid");
        var local = new LinkedHashMap<Long, DatasetTableFieldDTO>();
        var fields = node.getCurrentDsFields();
        if (fields == null || fields.isEmpty() || fields.size() > 256) throw AiErrors.error("invalid");
        for (var f : fields) {
            if (f == null || f.getId() == null || !Objects.equals(f.getExtField(), 0)
                    || !(allowed.contains(f.getOriginName()) || Objects.equals(savedColumns.get(f.getId()), f.getOriginName())) || !identifier(f.getOriginName())
                    || !Objects.equals(table.getId(), f.getDatasetTableId())
                    || !Objects.equals(table.getDatasourceId(), f.getDatasourceId())
                    || f.getChecked() == null || physical.putIfAbsent(f.getId(), f) != null) throw AiErrors.error("invalid");
            columns.put(f.getId(), new CalcFieldAccess.Column(table.getDatasourceId(), name, table.getId(), f.getOriginName()));
            local.put(f.getId(), f);
            clearAliases(f);
        }
        if (parent != null) {
            var join = node.getUnionToParent();
            if (join == null || join.getUnionType() == null || !Set.of("1:1", "1:N", "N:1", "inner", "left", "right", "full").contains(join.getUnionType())
                    || join.getUnionFields() == null || join.getUnionFields().isEmpty() || join.getUnionFields().size() > 16)
                throw AiErrors.error("invalid");
            for (var key : join.getUnionFields()) {
                if (key == null || key.getParentField() == null || key.getCurrentField() == null) throw AiErrors.error("invalid");
                var left = parent.get(key.getParentField().getId());
                var right = local.get(key.getCurrentField().getId());
                if (left == null || right == null) throw AiErrors.error("invalid");
                key.setParentField(left);
                key.setCurrentField(right);
            }
        }
        if (node.getChildrenDs() == null) node.setChildrenDs(List.of());
        for (var child : node.getChildrenDs()) node(child, local, depth + 1);
    }

    static void clearAliases(DatasetTableFieldDTO f) {
        f.setDataeaseName(null);
        f.setFieldShortName(null);
        f.setDbFieldName(null);
        if (f.getDateFormat() != null && !f.getDateFormat().matches("[a-zA-Z0-9 :/._%-]{0,64}")) throw AiErrors.error("invalid");
    }

    static boolean identifier(String value) {
        return value != null && value.matches("[\\p{L}\\p{N}_ $-]{1,128}");
    }

    static String json(Object value) {
        try { return JSON.writeValueAsString(value); }
        catch (java.io.IOException failure) { throw AiErrors.error("invalid"); }
    }

    static DatasetGroupInfoDTO copy(String json) {
        try { return JSON.readValue(json, DatasetGroupInfoDTO.class); }
        catch (java.io.IOException failure) { throw AiErrors.error("invalid"); }
    }
}
