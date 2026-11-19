package io.dataease.datasource.manage;

import io.dataease.datasource.dao.auto.repository.CoreDatasourceTableSemanticRepository;
import io.dataease.datasource.dao.auto.repository.CoreDatasourceFieldSemanticRepository;
import io.dataease.extensions.datasource.dto.DatasetTableDTO;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Initial copies only. Dataset persistence never resolves datasource semantics again. */
@Service
public class DatasourceSemanticSnapshot {
    private final CoreDatasourceTableSemanticRepository tables;
    private final CoreDatasourceFieldSemanticRepository fields;

    public DatasourceSemanticSnapshot(CoreDatasourceTableSemanticRepository tables,
                                      CoreDatasourceFieldSemanticRepository fields) {
        this.tables = tables;
        this.fields = fields;
    }

    @Transactional(readOnly = true)
    public List<DatasetTableDTO> tables(Long datasourceId, List<DatasetTableDTO> physical) {
        var semantics = tables.findByDatasource_IdOrderById(datasourceId).stream()
                .filter(table -> table.isActive() && table.isAvailable())
                .collect(Collectors.toMap(table -> table.getTableName(), Function.identity()));
        for (var table : physical) {
            var semantic = semantics.get(table.getTableName());
            if (semantic == null) continue;
            table.setSemanticDescription(semantic.getDescription());
            table.setSemanticGrain(semantic.getGrain());
            table.setSemanticConfirmed(semantic.isConfirmed());
        }
        return physical;
    }

    @Transactional(readOnly = true)
    public List<DatasetTableFieldDTO> fields(Long datasourceId, String tableName, List<DatasetTableFieldDTO> physical) {
        var table = tables.findByDatasource_IdAndTableName(datasourceId, tableName)
                .filter(value -> value.isActive() && value.isAvailable());
        if (table.isEmpty()) return physical;
        var semantics = fields.findByTableSemantic_Id(table.get().getId()).stream()
                .collect(Collectors.toMap(value -> value.getFieldName(), Function.identity()));
        for (var field : physical) {
            var semantic = semantics.get(field.getOriginName());
            if (semantic == null) continue;
            field.setSemanticBusinessName(semantic.getBusinessName());
            field.setSemanticDescription(semantic.getDescription());
            field.setSemanticAliases(semantic.getAliases());
            field.setSemanticValueReferences(semantic.getValueReferences());
            field.setSemanticConfirmed(semantic.isConfirmed());
        }
        return physical;
    }
}
