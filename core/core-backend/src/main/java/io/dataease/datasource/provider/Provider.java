package io.dataease.datasource.provider;


import io.dataease.dataset.dao.auto.entity.CoreDatasetTable;
import io.dataease.dataset.dao.auto.entity.CoreDatasetTableField;
import io.dataease.datasource.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.model.TableField;
import io.dataease.datasource.request.DatasourceRequest;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Provider {


    abstract public List<Map<String, String>> getTables(DatasourceRequest datasourceRequest) throws Exception;

    abstract public String checkStatus(DatasourceRequest datasourceRequest) throws Exception;
    abstract public List<TableField> getTableFields(DatasourceRequest datasourceRequest) throws Exception;

    public Map<String, List> fetchResultField(DatasourceRequest datasourceRequest) throws Exception {
        return new HashMap<>();
    }


    public List<String> getSchema(DatasourceRequest datasourceRequest) throws Exception {
        return null;
    }

    public void checkConfiguration(CoreDatasource datasource) throws Exception {
    }

    public String dsVersion(CoreDatasource datasource) throws Exception {
        return "";
    }
}
