package io.dataease.datasource.server;

import io.dataease.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.manage.DataSourceManage;
import io.dataease.datasource.manage.DatasourceMetadataAccess;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.DatasetTableDTO;
import io.dataease.extensions.datasource.dto.TableField;
import io.dataease.i18n.Translator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
class DatasourceMetadataSource {
    private final DatasourceServer datasource;
    private final DataSourceManage dataSources;

    DatasourceMetadataSource(DatasourceServer datasource, DataSourceManage dataSources) {
        this.datasource = datasource;
        this.dataSources = dataSources;
    }

    List<TableField> fields(CoreDatasource source, String tableName) {
        return datasource.getFieldsForListedTable(source, tableName);
    }

    Map<String, DatasetTableDTO> tables(Long datasourceId, CoreDatasource source) {
        return datasource.listPhysicalTables(datasourceId, source).stream().collect(Collectors.toMap(
                DatasetTableDTO::getTableName, Function.identity(), (first, duplicate) -> first));
    }

    static DatasetTableDTO listedTable(Map<String, DatasetTableDTO> tables, String tableName) {
        var table = tables.get(tableName);
        if (table == null) {
            DEException.throwException(Translator.get("i18n_datasource_metadata_table_unavailable", tableName));
        }
        return table;
    }

    CoreDatasource datasource(Long datasourceId) {
        var source = dataSources.getCoreDatasource(datasourceId);
        DatasourceMetadataAccess.requirePhysicalDatasource(source);
        return source;
    }

    static void requireText(String value, int length) {
        if (value == null || value.isBlank()) {
            DEException.throwException(Translator.get("i18n_datasource_metadata_required"));
        }
        limit(value, length);
    }

    static void limit(String value, int length) {
        if (value != null && value.length() > length) {
            DEException.throwException(Translator.get("i18n_datasource_metadata_length", length));
        }
    }
}
