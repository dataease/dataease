package io.dataease.datasource.manage;

import io.dataease.api.ds.DatasourceSemanticApi.*;
import io.dataease.datasource.dao.auto.entity.CoreDatasourceFieldSemantic;
import io.dataease.datasource.dao.auto.entity.CoreDatasourceTableSemantic;
import io.dataease.datasource.dao.auto.repository.CoreDatasourceFieldSemanticRepository;
import io.dataease.datasource.dao.auto.repository.CoreDatasourceTableSemanticRepository;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.TableField;
import io.dataease.i18n.Translator;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.utils.IDUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DatasourceSemanticManage {
    private final DatasourceMetadataAccess access;
    private final DatasourceRelationManage relations;
    private final CoreDatasourceTableSemanticRepository tableRepository;
    private final CoreDatasourceFieldSemanticRepository fieldRepository;

    public DatasourceSemanticManage(DatasourceMetadataAccess access, DatasourceRelationManage relations,
                                    CoreDatasourceTableSemanticRepository tableRepository,
                                    CoreDatasourceFieldSemanticRepository fieldRepository) {
        this.access = access;
        this.relations = relations;
        this.tableRepository = tableRepository;
        this.fieldRepository = fieldRepository;
    }

    @Transactional(readOnly = true)
    public Semantics list(Long datasourceId) {
        access.require(datasourceId);
        var tables = tableRepository.findByDatasource_IdOrderById(datasourceId).stream()
                .map(t -> new TableSemantic(t.getTableName(), t.getDescription(), t.getGrain(), t.isActive(),
                        t.getPhysicalLabel(), t.isAvailable())).toList();
        var fields = fieldRepository.findByActiveTable(datasourceId).stream()
                .map(f -> new FieldSemantic(f.getTableSemantic().getTableName(), f.getFieldName(),
                        f.getBusinessName(), f.getDescription(), f.getAliases(), f.isConfirmed(),
                        f.getPhysicalType(), f.getPhysicalComment())).toList();
        return new Semantics(tables, fields, relations.countByTable(datasourceId));
    }

    @Transactional
    public void saveTable(Long datasourceId, TableSemantic table) {
        access.lock(datasourceId);
        var row = activeTable(datasourceId, table.tableName());
        row.setDescription(table.description());
        row.setGrain(table.grain());
        row.setConfirmed(true);
    }

    @Transactional
    public void addTables(Long datasourceId, List<TableSnapshot> snapshots) {
        var datasource = access.lock(datasourceId);
        for (var snapshot : snapshots) {
            var row = table(datasourceId, snapshot.tableName());
            if (row != null && row.isActive()) {
                continue;
            }
            if (row == null) {
                row = new CoreDatasourceTableSemantic();
                row.setId(IDUtils.snowID());
                row.setCreateBy(Objects.toString(V3UserUtil.getUid(), null));
                row.setCreateTime(System.currentTimeMillis());
                row.setDatasource(datasource);
                row.setTableName(snapshot.tableName());
                row = tableRepository.save(row);
            }
            row.setActive(true);
            syncStructure(datasourceId, row, snapshot);
        }
    }

    @Transactional
    public void removeTable(Long datasourceId, String tableName) {
        access.lock(datasourceId);
        var row = table(datasourceId, tableName);
        if (row != null) {
            row.setActive(false);
        }
    }

    @Transactional
    public void refreshTable(Long datasourceId, String tableName, TableSnapshot snapshot) {
        access.lock(datasourceId);
        syncStructure(datasourceId, activeTable(datasourceId, tableName), snapshot);
    }

    private void syncStructure(Long datasourceId, CoreDatasourceTableSemantic row, TableSnapshot snapshot) {
        row.setAvailable(snapshot != null);
        if (snapshot == null) {
            relations.updateRelations(datasourceId, row.getTableName(), null);
            return;
        }
        row.setPhysicalLabel(snapshot.label());
        var existing = fieldRepository.findByTableSemantic_Id(row.getId()).stream()
                .collect(Collectors.toMap(CoreDatasourceFieldSemantic::getFieldName, field -> field));
        var names = new LinkedHashSet<String>();
        for (TableField source : snapshot.fields()) {
            if (!names.add(source.getOriginName())) {
                continue;
            }
            var field = existing.remove(source.getOriginName());
            boolean create = field == null;
            if (create) {
                field = new CoreDatasourceFieldSemantic();
                field.setId(IDUtils.snowID());
                field.setCreateBy(Objects.toString(V3UserUtil.getUid(), null));
                field.setCreateTime(System.currentTimeMillis());
                field.setTableSemantic(row);
                field.setFieldName(source.getOriginName());
            }
            field.setPosition(names.size() - 1);
            field.setPhysicalType(source.getFieldType() == null || source.getFieldType().isBlank()
                    ? source.getType() : source.getFieldType());
            field.setPhysicalComment(Objects.equals(source.getName(), source.getOriginName()) ? null : source.getName());
            if (create) {
                fieldRepository.save(field);
            }
        }
        fieldRepository.deleteAll(existing.values());
        relations.updateRelations(datasourceId, row.getTableName(), names);
    }

    @Transactional
    public void saveField(Long datasourceId, FieldSemantic field) {
        access.lock(datasourceId);
        var table = activeTable(datasourceId, field.tableName());
        var row = fieldRepository.findByTableSemantic_IdAndFieldName(table.getId(), field.fieldName()).orElse(null);
        if (row == null || !table.isAvailable()) {
            DEException.throwException(Translator.get("i18n_datasource_metadata_field_unavailable"));
        }
        row.setBusinessName(field.businessName());
        String description = field.description();
        row.setDescription(description == null || description.isBlank() ? null : description);
        row.setAliases(field.aliases());
        row.setConfirmed(true);
    }

    private CoreDatasourceTableSemantic activeTable(Long datasourceId, String tableName) {
        var row = table(datasourceId, tableName);
        if (row == null || !row.isActive()) {
            DEException.throwException(Translator.get("i18n_datasource_metadata_add_table_first"));
        }
        return row;
    }

    public record TableSnapshot(String tableName, String label, List<TableField> fields) {}

    private CoreDatasourceTableSemantic table(Long datasourceId, String tableName) {
        return tableRepository.findByDatasource_IdAndTableName(datasourceId, tableName).orElse(null);
    }
}
