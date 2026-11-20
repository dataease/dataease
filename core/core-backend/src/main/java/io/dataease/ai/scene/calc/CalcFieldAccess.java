package io.dataease.ai.scene.calc;

import io.dataease.ai.security.identity.AiAccess;
import io.dataease.ai.support.AiErrors;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.api.dataset.union.UnionDTO;
import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.api.permissions.auth.dto.BusiPerCheckDTO;
import io.dataease.api.permissions.dataset.api.ColumnPermissionsApi;
import io.dataease.api.permissions.dataset.api.RowPermissionsApi;
import io.dataease.constant.AuthEnum;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.dataset.manage.DatasetGroupManage;
import io.dataease.dataset.manage.DatasetTableFieldManage;
import io.dataease.dataset.manage.PermissionManage;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.view.dto.ColumnPermissionItem;
import io.dataease.extensions.view.dto.DatasetRowPermissionsTreeObj;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import io.dataease.resource.RootFolderCreatePermissionManage;
import io.dataease.system.manage.CorePermissionManage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class CalcFieldAccess {
    private final AiAccess identity;
    private final ObjectProvider<InteractiveAuthApi> auth;
    private final ObjectProvider<ColumnPermissionsApi> columns;
    private final ObjectProvider<RowPermissionsApi> rows;
    private final CorePermissionManage permissions;
    private final RootFolderCreatePermissionManage destinations;
    private final DatasetGroupManage groups;
    private final DatasetTableFieldManage fields;
    private final PermissionManage policies;

    public CalcFieldAccess(AiAccess identity, ObjectProvider<InteractiveAuthApi> auth,
                           ObjectProvider<ColumnPermissionsApi> columns, ObjectProvider<RowPermissionsApi> rows,
                           CorePermissionManage permissions, RootFolderCreatePermissionManage destinations,
                           DatasetGroupManage groups, DatasetTableFieldManage fields, PermissionManage policies) {
        this.identity = identity;
        this.auth = auth;
        this.columns = columns;
        this.rows = rows;
        this.permissions = permissions;
        this.destinations = destinations;
        this.groups = groups;
        this.fields = fields;
        this.policies = policies;
    }

    public record Column(Long source, String table, Long node, String column) {}
    public record Grant(Long savedVersion, Set<Long> hidden, Map<Long,String> aliases, boolean rowRestricted) {
        public Grant { hidden = Set.copyOf(hidden); aliases = Map.copyOf(aliases); }
    }

    DatasetGroupInfoDTO authorizedDataset(DatasetGroupInfoDTO draft) {
        try {
            boolean saved = draft.getId() != null;
            Long id = saved ? draft.getId() : draft.getPid();
            if (id == null || id < 0 || saved && id == 0) throw AiErrors.error("forbidden");
            var actor = identity.capture();
            identity.verify(actor, false);
            if (!saved) destinations.check(id, BusiResourceEnum.DATASET);
            if (saved || id != 0) {
                var request = new BusiNodeRequest();
                request.setBusiFlag("dataset");
                request.setWeight(AuthEnum.MANAGE.getWeight());
                if (!contains(groups.tree(request), id, saved)) throw AiErrors.error("forbidden");
            }
            if (saved) {
                var permission = new BusiPerCheckDTO(id, BusiResourceEnum.DATASET, AuthEnum.MANAGE);
                if (actor.deployment().organizations()) {
                    if (auth.getIfAvailable() == null || columns.getIfAvailable() == null || rows.getIfAvailable() == null)
                        throw AiErrors.error("forbidden");
                    auth.getObject().checkAuth(permission);
                } else if (!permissions.checkAuth(permission)) throw AiErrors.error("forbidden");
            }
            if (!saved) return null;
            var original = groups.getForCount(id);
            if (original == null || !"dataset".equals(original.getNodeType())) throw AiErrors.error("forbidden");
            return original;
        } catch (AiErrors.Failure known) { throw known; }
        catch (Exception unavailable) { throw AiErrors.error("forbidden"); }
    }

    public Grant grant(DatasetGroupInfoDTO draft, Map<Long, Column> bindings, long target) {
        return grant(draft, bindings, target, authorizedDataset(draft));
    }

    Grant grant(DatasetGroupInfoDTO draft, Map<Long, Column> bindings, long target, DatasetGroupInfoDTO original) {
        try {
            Long id = draft.getId();
            boolean saved = id != null;
            var ids = new HashSet<>(bindings.keySet());
            draft.getAllFields().forEach(f -> ids.add(f.getId()));
            var persisted = fields.selectByFieldIds(new ArrayList<>(ids));
            if (persisted.stream().anyMatch(f -> !saved || !Objects.equals(f.getDatasetGroupId(), id) || f.getChartId() != null))
                throw AiErrors.error("forbidden");
            if (!saved) return new Grant(null, Set.of(), Map.of(), false);
            var sourceColumns = new HashMap<Long,Column>();
            var savedFields = new LinkedHashMap<Long,DatasetTableFieldDTO>();
            for (var node : original.getUnion()) collect(node, sourceColumns, savedFields);
            original.getAllFields().forEach(f -> savedFields.put(f.getId(), f));
            persisted.forEach(f -> savedFields.put(f.getId(), f));
            var trusted = new ArrayList<>(savedFields.values());
            var aliases = new HashMap<Long,String>();
            var hidden = new HashSet<Long>();
            var masked = new HashMap<String, ColumnPermissionItem>();
            var allowed = policies.filterColumnPermissions(trusted, masked, id, null);
            var deniedColumns = new HashSet<List<Object>>();
            for (var field : trusted) {
                Long fid = field.getId();
                if (!allowed.contains(field) || masked.containsKey(field.getDataeaseName())) {
                    hidden.add(fid);
                    if (sourceColumns.containsKey(fid)) deniedColumns.add(physicalKey(sourceColumns.get(fid)));
                }
                if (bindings.containsKey(fid) && !Objects.equals(sourceColumns.get(fid), bindings.get(fid)))
                    throw AiErrors.error("forbidden");
                if (field.getDataeaseName() != null && !field.getDataeaseName().isBlank()) {
                    if (!CalcFieldCanvas.identifier(field.getDataeaseName())) throw AiErrors.error("forbidden");
                    aliases.put(fid, field.getDataeaseName());
                }
                if (Objects.equals(target, fid) && !Objects.equals(field.getExtField(), 2)) throw AiErrors.error("forbidden");
            }
            bindings.forEach((fid, column) -> { if (deniedColumns.contains(physicalKey(column))) hidden.add(fid); });
            if (hidden.contains(target)) throw AiErrors.error("forbidden");
            var rowRules = policies.getRowPermissionsTree(id, identity.capture().actor());
            for (var rule : rowRules)
                verifyRowFields(rule.getTree(), bindings, sourceColumns, draft);
            return new Grant(original.getLastUpdateTime(), hidden, aliases, !rowRules.isEmpty());
        } catch (AiErrors.Failure known) { throw known; }
        catch (Exception unavailable) { throw AiErrors.error("forbidden"); }
    }

    private List<Object> physicalKey(Column c) { return List.of(c.source(), c.table(), c.column()); }

    private boolean contains(List<BusiNodeVO> tree, long id, boolean leaf) {
        if (tree == null) return false;
        for (var node : tree) {
            if (Objects.equals(node.getId(), id) && Objects.equals(node.getLeaf(), leaf)) return true;
            if (contains(node.getChildren(), id, leaf)) return true;
        }
        return false;
    }

    private void collect(UnionDTO node, Map<Long, Column> columns, Map<Long, DatasetTableFieldDTO> savedFields) {
        var table = node.getCurrentDs();
        if (node.getCurrentDsFields() != null)
            for (var field : node.getCurrentDsFields()) {
                savedFields.put(field.getId(), field);
                columns.put(field.getId(), new Column(table.getDatasourceId(), table.getTableName(), table.getId(), field.getOriginName()));
            }
        if (node.getChildrenDs() != null) node.getChildrenDs().forEach(child -> collect(child, columns, savedFields));
    }

    private void verifyRowFields(DatasetRowPermissionsTreeObj tree, Map<Long, Column> bindings,
                                 Map<Long, Column> trusted, DatasetGroupInfoDTO draft) {
        for (var item : tree.getItems()) {
            if ("tree".equals(item.getType())) {
                verifyRowFields(item.getSubTree(), bindings, trusted, draft);
            } else {
                Long id = item.getFieldId();
                if (item.getField() == null || !Objects.equals(item.getField().getExtField(), 0)
                        || !bindings.containsKey(id) || !bindings.get(id).equals(trusted.get(id))
                        || draft.getAllFields().stream().noneMatch(f -> Objects.equals(id, f.getId())
                                && Objects.equals(f.getExtField(), 0) && Boolean.TRUE.equals(f.getChecked())))
                    throw AiErrors.error("forbidden");
            }
        }
    }
}
