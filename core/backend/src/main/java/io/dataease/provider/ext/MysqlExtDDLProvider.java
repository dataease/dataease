package io.dataease.provider.ext;

import io.dataease.commons.utils.BeanUtils;
import io.dataease.plugins.common.dto.datafill.ExtIndexField;
import io.dataease.plugins.common.dto.datafill.ExtTableField;
import io.dataease.plugins.common.dto.datasource.TableField;
import io.dataease.plugins.common.request.datasource.DatasourceRequest;
import io.dataease.plugins.datasource.provider.DefaultExtDDLProvider;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


@Service("extMysqlDDLProvider")
public class MysqlExtDDLProvider extends DefaultExtDDLProvider {

    private static final String creatTableSql =
            "CREATE TABLE `TABLE_NAME`" +
                    "Column_Fields" + " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;";

    private static final String creatTableIndexSql =
            "create index `INDEX_NAME` on `TABLE_NAME` (Column_Fields);";

    private static final String dropTableSql = "DROP TABLE IF EXISTS `TABLE_NAME`";

    @Override
    public String getLowerCaseTaleNames() {
        return "SHOW VARIABLES LIKE 'lower_case_table_names'";
    }

    @Override
    public String dropTableSql(String table) {
        return dropTableSql.replace("TABLE_NAME", table);
    }

    @Override
    public String createTableSql(String table, List<ExtTableField> formFields) {
        //check inject
        /*if (checkSqlInjection(table)) {
            throw new RuntimeException("包含SQL注入的参数，请检查参数！");
        }*/

        List<ExtTableField.TableField> fields = convertTableFields(true, formFields);
        String fieldSql = convertTableFieldsString(table, fields);
        return creatTableSql.replace("TABLE_NAME", table).replace("Column_Fields", fieldSql);
    }

    @Override
    public String addTableColumnSql(String table, List<ExtTableField> formFieldsToCreate, List<ExtTableField> formFieldsToModify) {
        String modifyTableSql = "ALTER TABLE `$TABLE_NAME$` $Column_Fields$ ;";
        List<ExtTableField.TableField> fields = convertTableFields(false, formFieldsToCreate);
        List<ExtTableField.TableField> fieldsToModify = convertTableFields(false, formFieldsToModify);
        String fieldSql = convertTableFieldsString(table, fields, true, fieldsToModify);
        return modifyTableSql.replace("$TABLE_NAME$", table).replace("$Column_Fields$", fieldSql);
    }

    @Override
    public String dropTableColumnSql(String table, List<ExtTableField> formFields) {
        String modifyTableSql = "ALTER TABLE `$TABLE_NAME$` $Column_Fields$ ;";
        List<ExtTableField.TableField> fields = convertTableFields(false, formFields);
        StringBuilder str = new StringBuilder();
        str.append("\n");
        for (int i = 0; i < fields.size(); i++) {
            ExtTableField.TableField field = fields.get(i);
            str.append("drop column ");
            str.append("`").append(field.getColumnName()).append("` ");
            if (i != fields.size() - 1) {
                str.append(",\n");
            }
        }
        return modifyTableSql.replace("$TABLE_NAME$", table).replace("$Column_Fields$", str.toString());
    }

    @Override
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

    @Override
    public String searchColumnData(String table, String column, String order, String query, boolean limit) {
        String baseSql = "SELECT DISTINCT `$Column_Field$` FROM `$TABLE_NAME$` ORDER BY `$Column_Field$` $Column_Order$";
        if (StringUtils.isNotBlank(query)) {
            baseSql = "SELECT `_tmp_value`\n" +
                    "FROM (SELECT DISTINCT CAST(`$Column_Field$` as CHAR) AS `_tmp_value` FROM `$TABLE_NAME$` ORDER BY `$Column_Field$` $Column_Order$) `_temp_table`\n" +
                    "WHERE `_tmp_value` LIKE '%" +
                    query.replaceAll(Pattern.quote("'"), "\\\\'")
                    + "%'";
        }
        baseSql = baseSql.replace("$TABLE_NAME$", table).replace("$Column_Field$", column).replace("$Column_Field$", column);
        if (StringUtils.equalsIgnoreCase(order, "desc")) {
            baseSql = baseSql.replace("$Column_Order$", "DESC");
        } else {
            baseSql = baseSql.replace("$Column_Order$", "ASC");
        }
        if (limit) {
            baseSql = baseSql + " LIMIT 1000;";
        } else {
            baseSql = baseSql + ";";
        }
        return baseSql;
    }

    @Override
    public String whereSql(String tableName, List<TableField> searchFields) {
        StringBuilder builder = new StringBuilder("WHERE 1 = 1 ");
        for (TableField searchField : searchFields) {
            //目前只考虑等于
            if (searchField.getInCount() > 1) {
                List<String> pList = new ArrayList<>();
                for (int i = 0; i < searchField.getInCount(); i++) {
                    pList.add("?");
                }
                String str = "AND $Column_Field$ IN (" + String.join(", ", pList) + ")";
                builder.append(str.replace("$Column_Field$", searchField.getFieldName()));
            } else {
                builder.append("AND $Column_Field$ = ? ".replace("$Column_Field$", searchField.getFieldName()));
            }
        }
        return builder.toString();
    }

    @Override
    public String countSql(String table, List<TableField> formFields, String whereSql) {
        String baseSql = "SELECT COUNT(1) FROM `$TABLE_NAME$` $WHERE_SQL$ ;";
        baseSql = baseSql.replace("$TABLE_NAME$", table);
        if (StringUtils.isBlank(whereSql)) {
            baseSql = baseSql.replace("$WHERE_SQL$", "");
        } else {
            baseSql = baseSql.replace("$WHERE_SQL$", whereSql);
        }
        return baseSql;
    }

    private String convertSearchFields(List<TableField> formFields) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < formFields.size(); i++) {
            TableField f = formFields.get(i);
            if (StringUtils.equalsAnyIgnoreCase(f.getFieldType(), "datetime")) {
                //特殊处理，全部使用统一格式输出
                builder.append("DATE_FORMAT(`").append(f.getFieldName()).append("`,'%Y-%m-%d %H:%i:%S')");
            } else {
                builder.append("`").append(f.getFieldName()).append("`");
            }
            if (i < formFields.size() - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    private List<ExtTableField.TableField> convertTableFields(boolean create, List<ExtTableField> formFields) {
        List<ExtTableField.TableField> list = new ArrayList<>();
        if (create) {
            list.add(
                    ExtTableField.TableField.builder()
                            .columnName("id")
                            .comment("ID")
                            .primaryKey(true)
                            .type(ExtTableField.BaseType.nvarchar)
                            .size(128)
                            .build()
            );
        }

        for (ExtTableField formField : formFields) {
            ExtTableField.TableField.TableFieldBuilder fieldBuilder = ExtTableField.TableField.builder()
                    .columnName(formField.getSettings().getMapping().getColumnName())
                    .oldColumnName(formField.getSettings().getMapping().getOldColumnName())
                    .type(formField.getSettings().getMapping().getType())
                    .comment(formField.getSettings().getName())
                    .required(formField.getSettings().isRequired());
            if (StringUtils.equalsIgnoreCase(formField.getType(), "dateRange")) {
                ExtTableField.TableField f1 = fieldBuilder
                        .columnName(formField.getSettings().getMapping().getColumnName1())
                        .oldColumnName(formField.getSettings().getMapping().getOldColumnName1())
                        .comment(formField.getSettings().getName() + " start")
                        .build();
                list.add(f1);

                ExtTableField.TableField f2 = BeanUtils.copyBean(new ExtTableField.TableField(), f1);
                f2.setColumnName(formField.getSettings().getMapping().getColumnName2());
                f2.setOldColumnName(formField.getSettings().getMapping().getOldColumnName2());
                f2.setComment(formField.getSettings().getName() + " end");
                list.add(f2);
            } else {
                list.add(fieldBuilder.build());
            }
        }

        return list;
    }

    @Override
    public String deleteDataByIdsSql(String table, List<DatasourceRequest.TableFieldWithValue> pks) {
        String deleteSql = "DELETE FROM `$TABLE_NAME$` WHERE `$PRIMARY_KEY$` IN ($PRIMARY_KEY_GROUP$);";
        StringBuilder groupStr = new StringBuilder();
        for (int i = 0; i < pks.size(); i++) {
            groupStr.append("?");
            if (i < pks.size() - 1) {
                groupStr.append(", ");
            }
        }
        return deleteSql.replace("$TABLE_NAME$", table)
                .replace("$PRIMARY_KEY$", pks.get(0).getFiledName())
                .replace("$PRIMARY_KEY_GROUP$", groupStr);
    }

    private String convertFields(List<DatasourceRequest.TableFieldWithValue> fields) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            DatasourceRequest.TableFieldWithValue f = fields.get(i);
            builder.append("`").append(f.getFiledName()).append("`");
            if (i < fields.size() - 1) {
                builder.append(", ");
            }
        }

        return builder.toString();
    }

    private String convertValueFields(List<DatasourceRequest.TableFieldWithValue> fields) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            DatasourceRequest.TableFieldWithValue f = fields.get(i);
            builder.append("?");
            if (i < fields.size() - 1) {
                builder.append(", ");
            }
        }

        return builder.toString();
    }

    private String convertInsertValueFields(List<DatasourceRequest.TableFieldWithValue> fields, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append("(").append(convertValueFields(fields)).append(")");
            if (i < count - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    @Override
    public String insertDataSql(String tableName, List<DatasourceRequest.TableFieldWithValue> fields, int count) {
        String sql = "INSERT INTO `$TABLE_NAME$`($Column_Fields$) VALUES $Value_Fields$;";
        return sql.replace("$TABLE_NAME$", tableName)
                .replace("$Column_Fields$", convertFields(fields))
                .replace("$Value_Fields$", convertInsertValueFields(fields, count));
    }

    private String convertUpdateFields(List<DatasourceRequest.TableFieldWithValue> fields) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            DatasourceRequest.TableFieldWithValue f = fields.get(i);
            builder.append("`").append(f.getFiledName()).append("` = ?");
            if (i < fields.size() - 1) {
                builder.append(", ");
            }
        }

        return builder.toString();
    }

    @Override
    public String updateDataByIdSql(String tableName, List<DatasourceRequest.TableFieldWithValue> fields, DatasourceRequest.TableFieldWithValue pk) {
        String sql = "UPDATE `$TABLE_NAME$` SET $Column_Fields$ WHERE `$PRIMARY_KEY$` = ?;";
        return sql.replace("$TABLE_NAME$", tableName)
                .replace("$Column_Fields$", convertUpdateFields(fields))
                .replace("$PRIMARY_KEY$", pk.getFiledName());
    }

    @Override
    public String checkUniqueValueSql(String tableName, DatasourceRequest.TableFieldWithValue field, DatasourceRequest.TableFieldWithValue pk) {
        String sql = "SELECT COUNT(1) FROM `$TABLE_NAME$` WHERE `$Column_Field$` = ? $PRIMARY_KEY_CONDITION$;";

        StringBuilder pkCondition = new StringBuilder();
        if (pk != null) {
            pkCondition.append("AND `").append(pk.getFiledName()).append("` != ?");
        }

        return sql.replace("$TABLE_NAME$", tableName)
                .replace("$Column_Field$", field.getFiledName())
                .replace("$PRIMARY_KEY_CONDITION$", pkCondition.toString());
    }

    private String convertTableFieldsString(String table, List<ExtTableField.TableField> fields) {
        return convertTableFieldsString(table, fields, false);
    }

    private String convertTableFieldsString(String table, List<ExtTableField.TableField> fields, boolean notCreateTable) {
        return convertTableFieldsString(table, fields, notCreateTable, null);
    }

    private String convertTableFieldsString(String table, List<ExtTableField.TableField> fields, boolean notCreateTable, List<ExtTableField.TableField> fieldsToModify) {
        StringBuilder str = new StringBuilder();
        if (notCreateTable) {
            str.append("\n");
        } else {
            str.append("(\n");
        }

        if (notCreateTable && CollectionUtils.isNotEmpty(fieldsToModify)) {
            for (int i = 0; i < fieldsToModify.size(); i++) {
                ExtTableField.TableField field = fieldsToModify.get(i);

                str.append("change ");

                //column name
                str.append("`").append(field.getOldColumnName()).append("` ");
                str.append("`").append(field.getColumnName()).append("` ");

                appendTypes(str, field);

                //换行
                if (i < fieldsToModify.size() - 1) {
                    str.append(",\n");
                }
            }

            if (CollectionUtils.isNotEmpty(fields)) {
                str.append(",\n");
            }
        }


        ExtTableField.TableField primaryKeyField = null;
        for (int i = 0; i < fields.size(); i++) {
            ExtTableField.TableField field = fields.get(i);
            if (field.isPrimaryKey()) {
                primaryKeyField = field;
            }

            //check inject
            /*if (checkSqlInjection(field.getColumnName())) {
                throw new RuntimeException("包含SQL注入的参数，请检查参数！");
            }*/
            if (notCreateTable) {
                str.append("add ");
            }

            //column name
            str.append("`").append(field.getColumnName()).append("` ");

            appendTypes(str, field);

            //换行
            if (i < fields.size() - 1) {
                str.append(",\n");
            } else {
                if (primaryKeyField != null) {
                    str.append(",\n");
                }
            }

        }

        if (!notCreateTable && primaryKeyField != null) {
            str.append("constraint `")
                    .append(table)
                    .append("_pk` ")
                    .append("PRIMARY KEY (")
                    .append("`")
                    .append(primaryKeyField.getColumnName())
                    .append("`)");
        }

        if (!notCreateTable) {
            str.append("\n)\n");
        }
        return str.toString();
    }

    private static void appendTypes(StringBuilder str, ExtTableField.TableField field) {
        //type
        switch (field.getType()) {
            case nvarchar:
                str.append("NVARCHAR(");
                if (field.getSize() != null && field.getSize() > 0) {
                    str.append(field.getSize());
                } else {
                    str.append(256);
                }
                str.append(") ");
                break;
            case number:
                str.append("BIGINT(");
                if (field.getSize() != null && field.getSize() > 0) {
                    str.append(field.getSize());
                } else {
                    str.append(20);
                }
                str.append(") ");
                break;
            case decimal:
                str.append("DECIMAL(");
                if (field.getSize() != null && field.getSize() > 0) {
                    str.append(field.getSize());
                } else {
                    str.append(20);
                }
                str.append(",");
                if (field.getAccuracy() != null && field.getAccuracy() >= 0) {
                    str.append(field.getAccuracy());
                } else {
                    str.append(8);
                }
                str.append(") ");
                break;
            case datetime:
                str.append("DATETIME ");
                break;
            default:
                str.append("LONGTEXT ");
                break;
        }

        //必填 考虑到表单编辑的情况，调整为代码判断
            /*if (field.isRequired()) {
                str.append("NOT NULL ");
            }*/

        //comment
        str.append("COMMENT '").append(field.getComment()).append("' ");
    }

    @Override
    public List<String> createTableIndexSql(String table, List<ExtIndexField> indexFields) {
        List<String> list = new ArrayList<>();
        for (ExtIndexField indexField : indexFields) {
            String c = convertTableIndexSql(table, indexField);
            if (StringUtils.isNotEmpty(c)) {
                list.add(c);
            }
        }
        return list;
    }

    @Override
    public List<String> dropTableIndexSql(String table, List<ExtIndexField> indexFields) {
        List<String> list = new ArrayList<>();
        for (ExtIndexField indexField : indexFields) {
            String sql = "drop index `$INDEX_NAME$` on `$TABLE_NAME$`;";
            list.add(sql.replace("$TABLE_NAME$", table).replace("$INDEX_NAME$", indexField.getName()));
        }
        return list;
    }

    private String convertTableIndexSql(String table, ExtIndexField indexField) {
        StringBuilder column = new StringBuilder();
        /*if (CollectionUtils.isEmpty(indexField.getColumns())) {
            return null;
        }*/

        //check inject
        /*if (checkSqlInjection(table) || checkSqlInjection(indexField.getName())) {
            throw new RuntimeException("包含SQL注入的参数，请检查参数！");
        }*/

        int count = 0;
        for (ExtIndexField.ColumnSetting indexFieldColumn : indexField.getColumns()) {
            count++;
            column.append("`").append(indexFieldColumn.getColumn()).append("` ");
            if (StringUtils.equalsIgnoreCase(indexFieldColumn.getOrder(), "asc")) {
                column.append("asc");
            } else if (StringUtils.equalsIgnoreCase(indexFieldColumn.getOrder(), "desc")) {
                column.append("desc");
            }
            if (count < indexField.getColumns().size()) {
                column.append(", ");
            }
        }

        return creatTableIndexSql.replace("TABLE_NAME", table).replace("INDEX_NAME", indexField.getName()).replace("Column_Fields", column.toString());
    }
}
