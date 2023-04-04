package io.dataease.datasource.provider;



import io.dataease.dataset.dao.auto.entity.CoreDatasetTable;
import io.dataease.dataset.dao.auto.entity.CoreDatasetTableField;
import io.dataease.datasource.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.model.TableField;
import io.dataease.datasource.request.DatasourceRequest;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public abstract class Provider {

    abstract public List<String[]> getData(DatasourceRequest datasourceRequest) throws Exception;

    abstract public List<Map<String, String>> getTables(DatasourceRequest datasourceRequest) throws Exception ;

    abstract public String checkStatus(DatasourceRequest datasourceRequest) throws Exception ;

    public List<String[]> fetchResult(DatasourceRequest datasourceRequest) throws Exception {
        return null;
    }

    abstract public List<TableField> fetchResultField(DatasourceRequest datasourceRequest) throws Exception ;

    abstract public Map<String, List> fetchResultAndField(DatasourceRequest datasourceRequest) throws Exception;

    public void handleDatasource(DatasourceRequest datasourceRequest, String type) throws Exception{
    }

    public List<String> getSchema(DatasourceRequest datasourceRequest) throws Exception{
        return null;
    }

    public List<TableField> getTableFields(DatasourceRequest datasourceRequest) throws Exception {
        return null;
    }

    public String getViewSql(DatasourceRequest datasourceRequest) throws Exception {
        return null;
    }

    public String getSchemaSql(DatasourceRequest datasourceRequest) throws Exception{
        return null;
    }

    public Connection getConnection(DatasourceRequest datasourceRequest) throws Exception{
        return null;
    }



    public Connection getConnectionFromPool(DatasourceRequest datasourceRequest) throws Exception {
        return null;
    }

    public void addToPool(DatasourceRequest datasourceRequest) throws PropertyVetoException, SQLException, Exception {
    }

    public void checkConfiguration(CoreDatasource datasource) throws Exception{}

    public String dsVersion(CoreDatasource datasource) throws Exception{return "";}
}
