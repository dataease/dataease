package io.dataease.provider.datasource;

import io.dataease.dto.datasource.TableFiled;
import io.dataease.controller.request.datasource.DatasourceRequest;

import java.util.List;
import java.util.Map;

public abstract class DatasourceProvider {

    private int resultLimit = 30000;

    abstract public List<String[]> getData(DatasourceRequest datasourceRequest) throws Exception;

    abstract public List<String> getTables(DatasourceRequest datasourceRequest) throws Exception;

    public void checkStatus(DatasourceRequest datasourceRequest) throws Exception {
        getData(datasourceRequest);
    }

    abstract public List<String[]> fetchResult(DatasourceRequest datasourceRequest) throws Exception;

    abstract public List<TableFiled> fetchResultField(DatasourceRequest datasourceRequest) throws Exception;

    abstract public Map<String, List> fetchResultAndField(DatasourceRequest datasourceRequest) throws Exception;

    abstract public void handleDatasource(DatasourceRequest datasourceRequest, String type) throws Exception;

    abstract public List<String> getSchema(DatasourceRequest datasourceRequest) throws Exception;

    public abstract List<TableFiled> getTableFileds(DatasourceRequest datasourceRequest) throws Exception;
}
