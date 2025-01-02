package io.dataease.plugins.datasource.provider;

import io.dataease.plugins.common.dto.datafill.ExtIndexField;
import io.dataease.plugins.common.dto.datafill.ExtTableField;
import io.dataease.plugins.common.dto.datasource.TableField;
import io.dataease.plugins.common.request.datasource.DatasourceRequest;

import java.util.List;
import java.util.regex.Pattern;

public class DefaultExtDDLProvider extends ExtDDLProvider {

    private final Pattern CHECK_INJECT_PATTERN = Pattern.compile("(.*\\=.*\\-\\-.*)|(.*(\\+).*)|(.*\\w+(%|\\$|#|&)\\w+.*)|(.*\\|\\|.*)|(.*\\s+(and|or)\\s+.*)|(.*\\b(select|update|union|and|or|delete|insert|trancate|char|into|substr|ascii|declare|exec|count|master|into|drop|execute|sleep|extractvalue|updatexml|substring|database|concat|rand|gtid_subset)\\b.*)");

    @Override
    public boolean checkSqlInjection(String sql) {
        return CHECK_INJECT_PATTERN.matcher(sql.toLowerCase()).find();
    }

    @Override
    public String createTableSql(String table, List<ExtTableField> formFields) {
        return null;
    }

    @Override
    public String addTableColumnSql(String table, List<ExtTableField> formFieldsToCreate, List<ExtTableField> formFieldsToModify) {
        return null;
    }

    @Override
    public String dropTableColumnSql(String table, List<ExtTableField> formFields) {
        return null;
    }

    @Override
    public List<String> createTableIndexSql(String table, List<ExtIndexField> indexFields) {
        return null;
    }

    @Override
    public List<String> dropTableIndexSql(String table, List<ExtIndexField> indexFields) {
        return null;
    }

    @Override
    public String dropTableSql(String table) {
        return null;
    }

    @Override
    public String searchSql(String table, List<TableField> formFields, String whereSql, long limit, long offset) {
        return null;
    }

    @Override
    public String searchColumnData(String table, String column, String order, String query, boolean limit) {
        return null;
    }

    @Override
    public String countSql(String table, List<TableField> formFields, String whereSql) {
        return null;
    }

    @Override
    public String deleteDataByIdsSql(String table, List<DatasourceRequest.TableFieldWithValue> pks) {
        return null;
    }

    @Override
    public String insertDataSql(String tableName, List<DatasourceRequest.TableFieldWithValue> fields, int count) {
        return null;
    }

    @Override
    public String updateDataByIdSql(String tableName, List<DatasourceRequest.TableFieldWithValue> fields, DatasourceRequest.TableFieldWithValue pk) {
        return null;
    }

    @Override
    public String checkUniqueValueSql(String tableName, DatasourceRequest.TableFieldWithValue field, DatasourceRequest.TableFieldWithValue pk) {
        return null;
    }

    @Override
    public String whereSql(String tableName, List<TableField> searchFields) {
        return null;
    }

    @Override
    public String getLowerCaseTaleNames() {
        return null;
    }
}
