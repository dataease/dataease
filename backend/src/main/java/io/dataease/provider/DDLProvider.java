package io.dataease.provider;

import io.dataease.base.domain.DatasetTableField;

import java.util.List;

/**
 * @Author gin
 * @Date 2021/5/17 4:19 下午
 */
public abstract class DDLProvider {
    public abstract String createView(String name, String viewSQL);

    public abstract String dropTable(String name);

    public abstract String dropView(String name);

    public abstract String replaceTable(String name);

    public abstract String createTableSql(String name, List<DatasetTableField> datasetTableFields);
}
