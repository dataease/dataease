package io.dataease.datasource.manage;

import io.dataease.api.ds.DatasourceSemanticApi.*;
import io.dataease.datasource.dao.auto.entity.CoreDatasourceFieldSemantic;
import io.dataease.datasource.dao.auto.entity.CoreDatasourceTableSemantic;
import io.dataease.datasource.dao.auto.repository.CoreDatasourceFieldSemanticRepository;
import io.dataease.datasource.dao.auto.repository.CoreDatasourceTableSemanticRepository;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.TableField;
import io.dataease.i18n.Translator;
import org.springframework.util.StringUtils;
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
        var counts = fieldRepository.countFieldsByDatasource(datasourceId).stream()
                .collect(Collectors.toMap(CoreDatasourceFieldSemanticRepository.TableFieldCounts::getTableId,
                        count -> count));
        var tables = tableRepository.findByDatasource_IdOrderById(datasourceId).stream()
                .map(t -> new TableSemantic(t.getId(), t.getTableName(), t.getDescription(), t.getGrain(), t.isActive(),
                        t.getPhysicalLabel(), t.isAvailable(), t.isConfirmed(),
                        counts.containsKey(t.getId()) ? counts.get(t.getId()).getFieldCount() : 0,
                        counts.containsKey(t.getId()) ? counts.get(t.getId()).getConfirmedFieldCount() : 0)).toList();
        return new Semantics(tables);
    }

    @Transactional(readOnly = true)
    public TableDetails detail(Long datasourceId, Long tableId) {
        access.require(datasourceId);
        var table = tableRepository.findByIdAndDatasource_IdAndActiveTrue(tableId, datasourceId)
                .orElseThrow(() -> DEException.getException(Translator.get("i18n_datasource_metadata_add_table_first")));
        var fields = fieldRepository.findByTableSemantic_IdOrderByPosition(table.getId()).stream()
                .map(this::fieldSemantic).toList();
        return new TableDetails(fields, relations.countByTable(datasourceId, table.getTableName()));
    }

    @Transactional(readOnly = true)
    public List<FieldSemantic> registeredFields(Long datasourceId) {
        access.require(datasourceId);
        return fieldRepository.findAvailableFields(datasourceId).stream()
                .map(this::fieldSemantic).toList();
    }

    private FieldSemantic fieldSemantic(CoreDatasourceFieldSemantic f) {
        return new FieldSemantic(f.getTableSemantic().getTableName(), f.getFieldName(), f.getBusinessName(),
                f.getDescription(), f.getAliases(), f.isConfirmed(), f.getPhysicalType(), f.getPhysicalComment(),
                io.dataease.utils.JsonUtil.parseList(f.getValueReferences(),
                        new com.fasterxml.jackson.core.type.TypeReference<List<ValueReference>>() {}));
    }

    @Transactional
    public void saveTable(Long datasourceId, TableSemantic table) {
        access.lock(datasourceId);
        var row = activeTable(datasourceId, table.tableName());
        row.setDescription(table.description());
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
            fieldRepository.deleteAll(fieldRepository.findByTableSemantic_Id(row.getId()));
            tableRepository.delete(row);
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
        var row = requireField(datasourceId, field.tableName(), field.fieldName(), true);
        row.setBusinessName(field.businessName());
        String description = field.description();
        row.setDescription(description == null || description.isBlank() ? null : description);
        row.setAliases(field.aliases());
        if (field.valueReferences() != null) {
            row.setValueReferences((String) io.dataease.utils.JsonUtil.toJSONString(field.valueReferences()));
        }
        row.setConfirmed(true);
    }

    @Transactional
    public void setTableConfirmed(Long datasourceId, String tableName, boolean confirmed) {
        access.lock(datasourceId);
        var row = activeTable(datasourceId, tableName);
        if (confirmed && !row.isAvailable()) {
            DEException.throwException(Translator.get("i18n_datasource_metadata_table_unavailable", tableName));
        }
        row.setConfirmed(confirmed);
    }

    @Transactional
    public void setFieldConfirmed(Long datasourceId, String tableName, String fieldName, boolean confirmed) {
        access.lock(datasourceId);
        var row = requireField(datasourceId, tableName, fieldName, confirmed);
        if (confirmed && !StringUtils.hasText(row.getBusinessName())) {
            DEException.throwException(Translator.get("i18n_datasource_metadata_required"));
        }
        row.setConfirmed(confirmed);
    }

    private CoreDatasourceFieldSemantic requireField(Long datasourceId, String tableName, String fieldName,
                                                     boolean requireAvailable) {
        var table = activeTable(datasourceId, tableName);
        var row = fieldRepository.findByTableSemantic_IdAndFieldName(table.getId(), fieldName).orElse(null);
        if (row == null || (requireAvailable && !table.isAvailable())) {
            DEException.throwException(Translator.get("i18n_datasource_metadata_field_unavailable"));
        }
        return row;
    }

    private CoreDatasourceTableSemantic activeTable(Long datasourceId, String tableName) {
        var row = table(datasourceId, tableName);
        if (row == null || !row.isActive()) {
            DEException.throwException(Translator.get("i18n_datasource_metadata_add_table_first"));
        }
        return row;
    }

    public record SemanticSuggestion(String description, String grain, List<FieldSuggestion> fields) {}

    public record FieldSuggestion(String fieldName, String businessName, String description, String aliases) {}

    @Transactional
    public int applySuggestion(Long datasourceId, TableSemantic snapshot, SemanticSuggestion suggestion, java.util.Set<String> physicalFields) {
        access.lock(datasourceId);
        var table = table(datasourceId, snapshot.tableName());
        if (table == null || !table.isActive() || !table.isAvailable() || !table.getId().equals(snapshot.id())) {
            return 0;
        }
        int count = 0;
        if (!table.isConfirmed()) {
            String description = fill(table.getDescription(), suggestion.description(), 4000);
            if (!Objects.equals(description, table.getDescription())) {
                table.setDescription(description);
                count++;
            }
        }
        var suggestions = new java.util.HashMap<String, FieldSuggestion>();
        if (suggestion.fields() != null) {
            for (var field : suggestion.fields()) {
                if (field != null && physicalFields.contains(field.fieldName())) {
                    suggestions.putIfAbsent(field.fieldName(), field);
                }
            }
        }
        for (var field : fieldRepository.findByTableSemantic_IdOrderByPosition(table.getId())) {
            var candidate = suggestions.get(field.getFieldName());
            if (candidate == null || field.isConfirmed()) continue;
            String name = fill(field.getBusinessName(), candidate.businessName(), 255);
            String description = fill(field.getDescription(), candidate.description(), 4000);
            String aliases = fill(field.getAliases(), candidate.aliases(), 1000);
            if (!Objects.equals(name, field.getBusinessName()) || !Objects.equals(description, field.getDescription())
                    || !Objects.equals(aliases, field.getAliases())) {
                field.setBusinessName(name);
                field.setDescription(description);
                field.setAliases(aliases);
                count++;
            }
        }
        return count;
    }

    private static String fill(String current, String candidate, int limit) {
        if (candidate != null && !candidate.isBlank()
                && candidate.strip().length() <= limit) {
            return candidate.strip();
        }
        return current;
    }

    public record TableSnapshot(String tableName, String label, List<TableField> fields) {}

    private CoreDatasourceTableSemantic table(Long datasourceId, String tableName) {
        return tableRepository.findByDatasource_IdAndTableName(datasourceId, tableName).orElse(null);
    }
}
