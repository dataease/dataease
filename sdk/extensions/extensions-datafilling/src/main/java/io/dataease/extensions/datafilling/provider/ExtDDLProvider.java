package io.dataease.extensions.datafilling.provider;


import io.dataease.extensions.datasource.dto.TableField;
import io.dataease.extensions.datafilling.dto.ExtIndexField;
import io.dataease.extensions.datafilling.dto.ExtTableField;
import io.dataease.extensions.datasource.dto.TableFieldWithValue;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取对应的sql
 */
public abstract class ExtDDLProvider {

    public final String DEFAULT_DATE_FORMAT_STR = "yyyy-MM-dd HH:mm:ss";

    public abstract String createTableSql(String table, List<ExtTableField> formFields);

    @Deprecated
    public String getTableFieldsSql(String table) {
        String sql = "SELECT * FROM `$TABLE_NAME$` LIMIT 0 OFFSET 0";
        return sql.replace("$TABLE_NAME$", table);
    }

    public abstract String addTableColumnSql(String table, List<ExtTableField> formFieldsToCreate, List<ExtTableField> formFieldsToModify);

    public abstract String dropTableColumnSql(String table, List<ExtTableField> formFields);

    @Deprecated
    public String searchSql(String table, List<TableField> formFields, String whereSql, long limit, long offset) {
        String baseSql = "SELECT $Column_Fields$ FROM `$TABLE_NAME$` $WHERE_SQL$ ;";
        if (limit > 0) {
            baseSql = "SELECT $Column_Fields$ FROM `$TABLE_NAME$` $WHERE_SQL$ LIMIT $OFFSET_COUNT$, $LIMIT_COUNT$ ;";
        }
        baseSql = baseSql.replace("$TABLE_NAME$", table)
                .replace("$OFFSET_COUNT$", Long.toString(offset))
                .replace("$LIMIT_COUNT$", Long.toString(limit));
        if (StringUtils.isBlank(whereSql)) {
            baseSql = baseSql.replace("$WHERE_SQL$", "");
        } else {
            baseSql = baseSql.replace("$WHERE_SQL$", whereSql);
        }
        baseSql = baseSql.replace("$Column_Fields$", convertSearchFields(formFields));
        return baseSql;
    }

    private String convertSearchFields(List<TableField> formFields) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < formFields.size(); i++) {
            TableField f = formFields.get(i);
            if (StringUtils.equalsAnyIgnoreCase(f.getFieldType(), "datetime")) {
                //特殊处理，全部使用统一格式输出
                builder.append("DATE_FORMAT(`").append(f.getOriginName()).append("`,'%Y-%m-%d %H:%i:%S')");
            } else {
                builder.append("`").append(f.getOriginName()).append("`");
            }
            if (i < formFields.size() - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    @Deprecated
    public String searchColumnData(String table, String column, String order) {
        String baseSql = "SELECT DISTINCT `$Column_Field$` FROM `$TABLE_NAME$` ORDER BY `$Column_Field$` $Column_Order$;";
        baseSql = baseSql.replace("$TABLE_NAME$", table).replace("$Column_Field$", column).replace("$Column_Field$", column);
        if (StringUtils.equalsIgnoreCase(order, "desc")) {
            baseSql = baseSql.replace("$Column_Order$", "DESC");
        } else {
            baseSql = baseSql.replace("$Column_Order$", "ASC");
        }
        return baseSql;
    }

    @Deprecated
    public String searchColumnRowDataOne(String table, List<TableField> searchFields, TableFieldWithValue tableFieldWithValue) {
        String baseSql = "SELECT $Column_Fields$ FROM `$TABLE_NAME$` WHERE `$Column_Field$` = ? LIMIT 1;";
        baseSql = baseSql
                .replace("$Column_Fields$", StringUtils.join(searchFields.stream().map(s -> "`" + s.getOriginName() + "`").toList(), ", "))
                .replace("$TABLE_NAME$", table)
                .replace("$Column_Field$", tableFieldWithValue.getFiledName());
        return baseSql;
    }

    @Deprecated
    public String countSql(String table, String whereSql) {
        String baseSql = "SELECT COUNT(1) FROM `$TABLE_NAME$` $WHERE_SQL$ ;";
        baseSql = baseSql.replace("$TABLE_NAME$", table);
        if (StringUtils.isBlank(whereSql)) {
            baseSql = baseSql.replace("$WHERE_SQL$", "");
        } else {
            baseSql = baseSql.replace("$WHERE_SQL$", whereSql);
        }
        return baseSql;
    }

    public abstract String dropTableSql(String table);

    public abstract List<String> createTableIndexSql(String table, List<ExtIndexField> indexFields);

    public abstract List<String> dropTableIndexSql(String table, List<ExtIndexField> indexFields);

    public abstract String deleteDataByIdsSql(String table, List<TableFieldWithValue> pks);

    public abstract String insertDataSql(String tableName, List<TableFieldWithValue> fields, int count);

    public abstract String updateDataByIdSql(String tableName, List<TableFieldWithValue> fields, TableFieldWithValue pk);

    @Deprecated
    public String checkUniqueValueSql(String tableName, TableFieldWithValue field, TableFieldWithValue pk) {
        String sql = "SELECT COUNT(1) FROM `$TABLE_NAME$` WHERE `$Column_Field$` = ? $PRIMARY_KEY_CONDITION$;";

        StringBuilder pkCondition = new StringBuilder();
        if (pk != null) {
            pkCondition.append("AND `").append(pk.getFiledName()).append("` != ?");
        }

        return sql.replace("$TABLE_NAME$", tableName)
                .replace("$Column_Field$", field.getFiledName())
                .replace("$PRIMARY_KEY_CONDITION$", pkCondition.toString());
    }

    @Deprecated
    public String whereSql(String tableName, List<TableField> searchFields) {
        StringBuilder builder = new StringBuilder("WHERE 1 = 1 ");
        for (TableField searchField : searchFields) {
            if (searchField.getInCount() > 1) {
                List<String> pList = new ArrayList<>();
                for (int i = 0; i < searchField.getInCount(); i++) {
                    pList.add("?");
                }
                String str = "AND $Column_Field$ IN (" + String.join(", ", pList) + ")";
                builder.append(str.replace("$Column_Field$", searchField.getOriginName()));
            } else {
                switch (searchField.getTerm()) {
                    case "not_eq":
                        builder.append(("AND $Column_Field$ " + "!=" + " ? ").replace("$Column_Field$", searchField.getOriginName()));
                        break;
                    case "lt":
                        builder.append(("AND $Column_Field$ " + "<" + " ? ").replace("$Column_Field$", searchField.getOriginName()));
                        break;
                    case "gt":
                        builder.append(("AND $Column_Field$ " + ">" + " ? ").replace("$Column_Field$", searchField.getOriginName()));
                        break;
                    case "le":
                        builder.append(("AND $Column_Field$ " + "<=" + " ? ").replace("$Column_Field$", searchField.getOriginName()));
                        break;
                    case "ge":
                        builder.append(("AND $Column_Field$ " + ">=" + " ? ").replace("$Column_Field$", searchField.getOriginName()));
                        break;
                    case "null":
                        builder.append("AND $Column_Field$ IS NULL ");
                        break;
                    case "not_null":
                        builder.append("AND $Column_Field$ IS NOT NULL ");
                        break;
                    default:
                        builder.append(("AND $Column_Field$ " + "=" + " ? ").replace("$Column_Field$", searchField.getOriginName()));
                        break;
                }

            }
        }
        return builder.toString();
    }

    @Deprecated
    public String getLowerCaseTaleNames() {
        return "SHOW VARIABLES LIKE 'lower_case_table_names'";
    }

    public abstract Integer getColumnType(String name);

    public abstract String truncateTable(String table);

    public abstract String listAllIds(String table, String keyColumn);

}
