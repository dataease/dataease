package io.dataease.extensions.datasource.provider;

import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.DatasetTableDTO;
import io.dataease.extensions.datasource.dto.DatasourceDTO;
import io.dataease.extensions.datasource.dto.DatasourceRequest;
import io.dataease.extensions.datasource.dto.TableField;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * @Author Junjun
 */
public abstract class Provider {
    public abstract List<String> getSchema(DatasourceRequest datasourceRequest);

    public abstract List<DatasetTableDTO> getTables(DatasourceRequest datasourceRequest);

    public abstract Connection getConnection(DatasourceDTO coreDatasource) throws DEException;

    public abstract String checkStatus(DatasourceRequest datasourceRequest) throws Exception;

    public abstract Map<String, Object> fetchResultField(DatasourceRequest datasourceRequest) throws DEException;

    public abstract List<TableField> fetchTableField(DatasourceRequest datasourceRequest) throws DEException;
}
