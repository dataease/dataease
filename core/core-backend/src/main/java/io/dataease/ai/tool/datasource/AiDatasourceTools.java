package io.dataease.ai.tool.datasource;

import io.dataease.ai.engine.runtime.AiToolProtocol;
import io.dataease.ai.security.identity.AiAccess;
import io.dataease.ai.support.AiErrors;
import io.dataease.ai.tool.AiTool;

import io.dataease.api.ds.DatasourceApi;
import io.dataease.datasource.provider.PhysicalTableCatalog;
import io.dataease.datasource.server.DatasourceMetadataSource;
import io.dataease.extensions.datasource.dto.TableField;
import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.api.permissions.auth.dto.BusiPerCheckDTO;
import io.dataease.constant.AuthEnum;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.extensions.datasource.dto.DatasourceDTO;
import io.dataease.extensions.datasource.dto.DatasetTableDTO;
import io.dataease.extensions.datasource.dto.DatasetTableFieldRequest;
import io.dataease.api.ai.runtime.AiPromptFingerprint;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import io.dataease.system.manage.CorePermissionManage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import java.util.*;

/** Shared authorized metadata operations and explicit model tools. No candidate SQL or data rows. */
@Component
public class AiDatasourceTools {
    public record Resource(String id, String name) { }
    public static final class Scope {
        private final String resourceId, revision;
        private final boolean isCross;
        private final TurnCatalog turnCatalog;

        public Scope(String resourceId, String revision) { this(resourceId, revision, false); }
        public Scope(String resourceId, String revision, boolean isCross) { this(resourceId, revision, isCross, null); }
        private Scope(String resourceId, String revision, boolean isCross, TurnCatalog turnCatalog) {
            this.resourceId = resourceId;
            this.revision = revision;
            this.isCross = isCross;
            this.turnCatalog = turnCatalog;
        }
        public static Scope editor(String id, String revision, boolean cross) {
            return new Scope(id, revision, cross, new TurnCatalog());
        }
        public String resourceId() { return resourceId; }
        public String revision() { return revision; }
        public boolean isCross() { return isCross; }
    }

    private static final class TurnCatalog {
        private PhysicalTableCatalog directory;
        private final Map<String, TableFields> fields = new HashMap<>();
    }
    public record Datasource(String id, String name, String revision, String type) { }
    public record Table(String name, String comment) { }
    public record Field(String name, String type, String comment) { }
    public record TableFields(String table, List<Field> fields) {
        /** Preserve the canonical projection used by persisted structure snapshots. */
        public Map<String, Object> projection() {
            var result = new TreeMap<String, Object>();
            result.put("table", table);
            result.put("fields", fields.stream().map(field -> new TreeMap<>(Map.of(
                    "name", field.name(), "type", field.type(), "comment", field.comment()))).toList());
            return result;
        }
    }
    public record Selection(Scope datasource, List<String> tables) {
        public Selection { tables = List.copyOf(tables); }
    }
    @org.springframework.beans.factory.annotation.Autowired
    private io.dataease.datasource.manage.DatasourceSemanticManage semantics;
    @org.springframework.beans.factory.annotation.Autowired
    private DatasourceMetadataSource physical;
    protected PhysicalTableCatalog openCatalog(long id) { return physical.openCatalog(id); }

    private PhysicalTableCatalog directory(Scope scope) {
        authorize(scope);
        var turn = Objects.requireNonNull(scope.turnCatalog);
        if (turn.directory == null) {
            var catalog = openCatalog(Long.parseLong(scope.resourceId()));
            authorize(scope);
            turn.directory = catalog;
        }
        return turn.directory;
    }

    public boolean hasPhysicalTable(Scope scope, String table) {
        return directory(scope).labels().containsKey(table);
    }

    private final DatasourceApi datasources;
    private final ObjectProvider<InteractiveAuthApi> authorization;
    private final AiAccess access;
    private final CorePermissionManage permissions;
    public AiDatasourceTools(DatasourceApi datasources, ObjectProvider<InteractiveAuthApi> authorization,
                             AiAccess access, CorePermissionManage permissions) {
        this.datasources = datasources; this.authorization = authorization;
        this.access = access; this.permissions = permissions;
    }
    public List<Resource> resources() {
        var request = new BusiNodeRequest(); request.setBusiFlag("datasource"); request.setWeight(AuthEnum.READ.getWeight());
        var result = new ArrayList<Resource>(); collect(datasources.tree(request), result); return result;
    }
    private void collect(List<BusiNodeVO> nodes, List<Resource> result) {
        if (nodes == null) return;
        for (var node : nodes) {
            if (!node.isOrgRoot() && Boolean.TRUE.equals(node.getLeaf()) && node.getId() != null && node.getId() > 0)
                result.add(new Resource(node.getId().toString(), node.getName()));
            collect(node.getChildren(), result);
        }
    }
    public Datasource resolve(String resourceId) {
        var source = load(resourceId);
        return new Datasource(resourceId, source.getName(), revision(source), source.getType());
    }
    public Datasource authorize(Scope scope) {
        var source = resolve(scope.resourceId());
        if (!Objects.equals(source.revision(), scope.revision())) throw AiErrors.error("scope_changed");
        return source;
    }
    public void authorizeUnbound() { var identity = access.capture(); access.verify(identity, false); }

    private List<DatasetTableDTO> authorizedTables(Scope scope) {
        authorize(scope);
        var request = new DatasetTableDTO(); request.setDatasourceId(Long.valueOf(scope.resourceId()));
        var result = datasources.getTables(request);
        if (result == null || result.size() > 256) throw AiErrors.error("output_limit");
        authorize(scope);
        return result;
    }
    public List<Table> tables(Scope scope) {
        if (scope.turnCatalog != null)
            return editorTableLabels(scope).entrySet().stream().map(e -> new Table(e.getKey(), e.getValue())).toList();
        return authorizedTables(scope).stream().map(table -> new Table(safe(table.getTableName(), 256), safe(table.getName(), 512))).toList();
    }
    Map<String, String> editorTableLabels(Scope scope) {
        return directory(scope).labels();
    }
    /** Preserve validator semantics: absent exact table is null; ambiguous case is an empty list. */
    public List<String> validationFields(Scope scope, String table, Runnable active) {
        active.run();
        var tables = authorizedTables(scope);
        active.run();
        if (tables.stream().noneMatch(value -> Objects.equals(table, value.getTableName()))) {
            if (tables.stream().anyMatch(value -> table.equalsIgnoreCase(value.getTableName()))) return List.of();
            return null;
        }
        // authorizedTables already rechecked authority after metadata IO.
        var fields = datasources.getTableField(new DatasetTableFieldRequest(Long.valueOf(scope.resourceId()), table));
        active.run();
        authorize(scope);
        if (fields == null || fields.size() > 256) throw AiErrors.error("output_limit");
        return fields.stream().map(field -> safe(field.getOriginName() == null ? field.getName() : field.getOriginName(), 256)).toList();
    }
    public TableFields fields(Scope scope, String table) {
        return fields(scope, table, false);
    }
    public TableFields fields(Scope scope, String table, boolean nativeType) {
        if (scope.turnCatalog != null && nativeType) {
            var catalog = directory(scope);
            if (!catalog.labels().containsKey(table)) throw AiErrors.error("forbidden");
            var current = scope.turnCatalog.fields.get(table);
            if (current == null) {
                current = projectFields(table, catalog.fields(table), true);
                authorize(scope);
                scope.turnCatalog.fields.put(table, current);
            }
            return current;
        }
        if (table == null || table.length() > 256 || authorizedTables(scope).stream().noneMatch(value -> Objects.equals(table, value.getTableName())))
            throw AiErrors.error("forbidden");
        // authorizedTables already rechecked authority after metadata IO.
        var fields = datasources.getTableField(new DatasetTableFieldRequest(Long.valueOf(scope.resourceId()), table));
        var projection = projectFields(table, fields, nativeType);
        authorize(scope);
        return projection;
    }

    public TableFields namedFields(Scope scope, String table, List<String> names) {
        var catalog = directory(scope);
        if (!catalog.labels().containsKey(table)) throw AiErrors.error("forbidden");
        if (names.isEmpty()) return new TableFields(table, List.of());
        scope.turnCatalog.fields.remove(table);
        var fields = catalog.fields(table);
        authorize(scope);
        if (fields == null) throw AiErrors.error("output_limit");
        var selected = fields.stream()
                .filter(field -> names.contains(Objects.toString(field.getOriginName(), field.getName())))
                .toList();
        return projectFields(table, selected, true);
    }

    private TableFields projectFields(String table, List<TableField> fields, boolean nativeType) {
        if (fields == null || fields.size() > 256) throw AiErrors.error("output_limit");
        return new TableFields(table, fields.stream().map(field -> new Field(
                safe(field.getOriginName() == null ? field.getName() : field.getOriginName(), 256),
                safe(nativeType && field.getFieldType() != null && !field.getFieldType().isBlank() ? field.getFieldType() : field.getType(), 128), safe(field.getName(), 512))).toList());
    }
    public String structures(Selection selection, Runnable active) {
        if (selection.tables().isEmpty() || selection.tables().size() > 32) throw AiErrors.error("invalid");
        var result = new TreeMap<String, String>();
        for (String name : selection.tables()) {
            active.run();
            result.put(name, AiToolProtocol.result(fields(selection.datasource(), name).projection()));
            active.run();
        }
        return AiToolProtocol.result(result);
    }
    public AiTool<Scope> listTables() {
        return new AiTool<>(AiTool.readOnly("tools/datasource.md", "list_tables"),
                (scope, arguments, active, deadline) -> AiToolProtocol.result(Map.of("tables", tables(scope))));
    }
    private Object semanticFields(Scope scope, String table) {
        var physical = fields(scope, table);
        if (semantics == null) return physical;
        var selected = semantics.list(Long.valueOf(scope.resourceId())).tables().stream()
                .filter(value -> value.active() && value.available() && table.equals(value.tableName()))
                .findFirst();
        if (selected.isEmpty()) return physical;
        var names = physical.fields().stream().map(Field::name).collect(java.util.stream.Collectors.toSet());
        var confirmed = semantics.detail(Long.valueOf(scope.resourceId()), selected.get().id()).fields().stream()
                .filter(value -> value.confirmed() && names.contains(value.fieldName())).toList();
        authorize(scope);
        return Map.of("structure", physical, "confirmedFieldSemantics", confirmed);
    }

    public AiTool<Scope> getTableFields() {
        return new AiTool<>(AiTool.readOnly("tools/datasource.md", "get_table_fields", "table"),
                (scope, arguments, active, deadline) -> AiToolProtocol.result(semanticFields(scope, arguments.get("table"))));
    }
    public AiTool<Selection> inspectStructure() {
        return new AiTool<>(AiTool.readOnly("tools/datasource.md", "inspect_structure"),
                (selection, arguments, active, deadline) -> structures(selection, active));
    }
    private String safe(String value, int limit) {
        if (value == null) return "";
        if (value.length() > limit) throw AiErrors.error("output_limit");
        return value;
    }
    private DatasourceDTO load(String resourceId) {
        if (resourceId == null || !resourceId.matches("[0-9]{1,20}")) throw AiErrors.error("invalid");
        long id;
        try { id = Long.parseLong(resourceId); } catch (NumberFormatException e) { throw AiErrors.error("invalid"); }
        var identity = access.capture();
        access.verify(identity, false);
        // tree 沿原业务路径取得当前工作空间范围；可见不代替对应操作授权。
        if (resources().stream().noneMatch(resource -> resource.id().equals(resourceId))) throw AiErrors.error("forbidden");
        var request = new BusiPerCheckDTO(id, BusiResourceEnum.DATASOURCE, AuthEnum.READ);
        if (identity.deployment().organizations()) {
            // 许可模式仍要求真实权限提供者；缺失或拒绝不能落入社区默认实现。
            var auth = authorization.getIfAvailable();
            if (auth == null) throw AiErrors.error("forbidden");
            auth.checkAuth(request);
        } else if (!permissions.checkAuth(request)) {
            // 已验证的社区身份沿用 SQL 业务的模式感知授权入口；不调用仅许可模式可用的端点。
            throw AiErrors.error("forbidden");
        }
        access.verify(identity, false);
        // 操作授权必须显式执行：内部 Java 调用不触发 HTTP DePermit 拦截。
        var resource = datasources.getSimpleDs(id);
        if (resource == null || !Objects.equals(resource.getId(), id)) throw AiErrors.error("forbidden");
        access.verify(identity, false);
        return resource;
    }
    private String revision(DatasourceDTO resource) {
        return AiPromptFingerprint.of(new Prompt(resource.getId() + ":" + resource.getUpdateTime() + ":" + resource.getDsVersion()
                + ":" + resource.getType() + ":" + resource.getName()));
    }
}
