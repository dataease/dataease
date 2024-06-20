package io.dataease.datasource.provider;

import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.api.ds.vo.TableField;
import io.dataease.datasource.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.exception.DEException;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * @Author Junjun
 */
public abstract class Provider {
    public abstract List<String> getSchema(DatasourceRequest datasourceRequest);

    public abstract List<DatasetTableDTO> getTables(DatasourceRequest datasourceRequest);

    public abstract Connection getConnection(CoreDatasource coreDatasource) throws DEException;

    public abstract String checkStatus(DatasourceRequest datasourceRequest) throws Exception;

    public abstract Map<String, Object> fetchResultField(DatasourceRequest datasourceRequest) throws DEException;

    public abstract List<TableField> fetchTableField(DatasourceRequest datasourceRequest) throws DEException;
}
