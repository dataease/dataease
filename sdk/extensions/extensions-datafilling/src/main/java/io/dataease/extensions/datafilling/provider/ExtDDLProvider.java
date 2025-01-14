package io.dataease.extensions.datafilling.provider;


import io.dataease.extensions.datasource.dto.TableField;
import io.dataease.extensions.datafilling.dto.ExtIndexField;
import io.dataease.extensions.datafilling.dto.ExtTableField;
import io.dataease.extensions.datasource.dto.TableFieldWithValue;

import java.util.List;

/**
 * 获取对应的sql
 */
public abstract class ExtDDLProvider {

    public final String DEFAULT_DATE_FORMAT_STR = "yyyy-MM-dd HH:mm:ss";

    public abstract String createTableSql(String table, List<ExtTableField> formFields);

    public abstract String getTableFieldsSql(String table);

    public abstract String addTableColumnSql(String table, List<ExtTableField> formFieldsToCreate, List<ExtTableField> formFieldsToModify);

    public abstract String dropTableColumnSql(String table, List<ExtTableField> formFields);

    public abstract String searchSql(String table, List<TableField> formFields, String whereSql, long limit, long offset);

    public abstract String searchColumnData(String table, String column, String order);

    public abstract String searchColumnRowDataOne(String table, List<TableField> searchFields, TableFieldWithValue tableFieldWithValue);

    public abstract String countSql(String table, String whereSql);

    public abstract String dropTableSql(String table);

    public abstract List<String> createTableIndexSql(String table, List<ExtIndexField> indexFields);

    public abstract List<String> dropTableIndexSql(String table, List<ExtIndexField> indexFields);

    public abstract String deleteDataByIdsSql(String table, List<TableFieldWithValue> pks);

    public abstract String insertDataSql(String tableName, List<TableFieldWithValue> fields, int count);

    public abstract String updateDataByIdSql(String tableName, List<TableFieldWithValue> fields, TableFieldWithValue pk);

    public abstract String checkUniqueValueSql(String tableName, TableFieldWithValue field, TableFieldWithValue pk);

    public abstract String whereSql(String tableName, List<TableField> searchFields);

    public abstract String getLowerCaseTaleNames();

    public abstract Integer getColumnType(String name);

}
