package io.dataease.provider;

import io.dataease.base.domain.DatasetTableField;

import java.util.List;

public class DDLProviderImpl extends DDLProvider {
    @Override
    public String createView(String name, String viewSQL) {
        return null;
    }

    @Override
    public String dropTable(String name) {
        return null;
    }

    @Override
    public String dropView(String name) {
        return null;
    }

    @Override
    public String replaceTable(String name) {
        return null;
    }

    @Override
    public String createTableSql(String name, List<DatasetTableField> datasetTableFields) {
        return null;
    }


}
