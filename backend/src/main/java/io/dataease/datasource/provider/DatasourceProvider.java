package io.dataease.datasource.provider;

import io.dataease.datasource.dto.TableFiled;
import io.dataease.datasource.request.DatasourceRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class DatasourceProvider {

    private int resultLimit = 30000;

    abstract public List<String[]> getData(DatasourceRequest datasourceRequest) throws Exception;

    abstract public List<String> getTables(DatasourceRequest datasourceRequest) throws Exception;

    public List<TableFiled> getTableFileds(DatasourceRequest datasourceRequest) throws Exception {
        return new ArrayList<>();
    };

    public void checkStatus(DatasourceRequest datasourceRequest) throws Exception {
        getData(datasourceRequest);
    }

    abstract public List<String[]> fetchResult(DatasourceRequest datasourceRequest) throws Exception;

    abstract public List<TableFiled> fetchResultField(DatasourceRequest datasourceRequest) throws Exception;

    abstract public Map<String, List> fetchResultAndField(DatasourceRequest datasourceRequest) throws Exception;

    abstract public void handleDatasource(DatasourceRequest datasourceRequest, String type) throws Exception;

    abstract public List<String> getSchema(DatasourceRequest datasourceRequest) throws Exception;
}
