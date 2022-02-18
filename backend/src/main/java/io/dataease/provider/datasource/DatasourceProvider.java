package io.dataease.provider.datasource;

import io.dataease.dto.datasource.TableDesc;
import io.dataease.dto.datasource.TableField;
import io.dataease.controller.request.datasource.DatasourceRequest;

import java.util.List;
import java.util.Map;

public abstract class DatasourceProvider {

    private int resultLimit = 30000;

    abstract public List<String[]> getData(DatasourceRequest datasourceRequest) throws Exception;

    abstract public List<TableDesc> getTables(DatasourceRequest datasourceRequest) throws Exception;

    public String checkStatus(DatasourceRequest datasourceRequest) throws Exception {
        getData(datasourceRequest);
        return "Success";
    }

    abstract public List<String[]> fetchResult(DatasourceRequest datasourceRequest) throws Exception;

    abstract public List<TableField> fetchResultField(DatasourceRequest datasourceRequest) throws Exception;

    abstract public Map<String, List> fetchResultAndField(DatasourceRequest datasourceRequest) throws Exception;

    abstract public void handleDatasource(DatasourceRequest datasourceRequest, String type) throws Exception;

    abstract public List<String> getSchema(DatasourceRequest datasourceRequest) throws Exception;

    public abstract List<TableField> getTableFileds(DatasourceRequest datasourceRequest) throws Exception;
}
