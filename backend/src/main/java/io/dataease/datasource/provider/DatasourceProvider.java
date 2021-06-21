package io.dataease.datasource.provider;

import io.dataease.base.domain.DatasetTableField;
import io.dataease.base.domain.Datasource;
import io.dataease.datasource.dto.TableFiled;
import io.dataease.datasource.request.DatasourceRequest;

import java.sql.ResultSet;
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

    public void test(DatasourceRequest datasourceRequest) throws Exception {
        getData(datasourceRequest);
    }

    abstract public List<String[]> fetchResult(DatasourceRequest datasourceRequest) throws Exception;

    abstract public List<TableFiled> fetchResultField(DatasourceRequest datasourceRequest) throws Exception;

    abstract public Map<String, List> fetchResultAndField(DatasourceRequest datasourceRequest) throws Exception;

    abstract public void initDataSource(DatasourceRequest datasourceRequest, String type) throws Exception;
}
