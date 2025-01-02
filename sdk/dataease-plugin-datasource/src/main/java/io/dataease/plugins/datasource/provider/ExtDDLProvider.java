package io.dataease.plugins.datasource.provider;

import io.dataease.plugins.common.dto.datafill.ExtIndexField;
import io.dataease.plugins.common.dto.datafill.ExtTableField;
import io.dataease.plugins.common.dto.datasource.TableField;
import io.dataease.plugins.common.request.datasource.DatasourceRequest;

import java.util.List;

public abstract class ExtDDLProvider {

    public final String DEFAULT_DATE_FORMAT_STR = "yyyy-MM-dd HH:mm:ss";

    public abstract boolean checkSqlInjection(String sql);

    public abstract String createTableSql(String table, List<ExtTableField> formFields);

    public abstract String addTableColumnSql(String table, List<ExtTableField> formFieldsToCreate, List<ExtTableField> formFieldsToModify);

    public abstract String dropTableColumnSql(String table, List<ExtTableField> formFields);

    public abstract String searchSql(String table, List<TableField> formFields, String whereSql, long limit, long offset);

    public abstract String searchColumnData(String table, String column, String order, String query, boolean limit);

    public abstract String countSql(String table, List<TableField> formFields, String whereSql);

    public abstract String dropTableSql(String table);

    public abstract List<String> createTableIndexSql(String table, List<ExtIndexField> indexFields);

    public abstract List<String> dropTableIndexSql(String table, List<ExtIndexField> indexFields);

    public abstract String deleteDataByIdsSql(String table, List<DatasourceRequest.TableFieldWithValue> pks);

    public abstract String insertDataSql(String tableName, List<DatasourceRequest.TableFieldWithValue> fields, int count);

    public abstract String updateDataByIdSql(String tableName, List<DatasourceRequest.TableFieldWithValue> fields, DatasourceRequest.TableFieldWithValue pk);

    public abstract String checkUniqueValueSql(String tableName, DatasourceRequest.TableFieldWithValue field, DatasourceRequest.TableFieldWithValue pk);

    public abstract String whereSql(String tableName, List<TableField> searchFields);

    public abstract String getLowerCaseTaleNames();
}
