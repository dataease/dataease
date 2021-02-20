package io.dataease.datasource.provider;

import java.util.List;

public abstract class DatasourceProvider {

    protected String dataSourceConfigration;
    protected String type;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    protected String query;
    private int resultLimit = 30000;

    public String getDataSourceConfigration() {
        return dataSourceConfigration;
    }

    public void setDataSourceConfigration(String dataSourceConfigration) {
        this.dataSourceConfigration = dataSourceConfigration;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }



    abstract public List<String[]> getData() throws Exception;

    abstract public List<String> getTables() throws Exception;

    public void test() throws Exception {
        getData();
    }

}
