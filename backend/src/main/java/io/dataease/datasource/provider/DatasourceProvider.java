package io.dataease.datasource.provider;

import io.dataease.base.domain.Datasource;
import io.dataease.datasource.dto.TableFiled;

import java.util.ArrayList;
import java.util.List;

public abstract class DatasourceProvider {

    protected String query;
    private int resultLimit = 30000;
    protected Datasource datasource;


    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Datasource getDatasource() {
        return datasource;
    }

    public void setDatasource(Datasource datasource) {
        this.datasource = datasource;
    }

    abstract public List<String[]> getData() throws Exception;

    abstract public List<String> getTables() throws Exception;

    public List<TableFiled> getTableFileds(String table) throws Exception{
        return new ArrayList<>();
    };

    public void test() throws Exception {
        getData();
    }

}
