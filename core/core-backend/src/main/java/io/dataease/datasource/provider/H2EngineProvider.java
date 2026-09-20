package io.dataease.datasource.provider;


import io.dataease.dataset.utils.TableUtils;
import io.dataease.datasource.dao.auto.entity.CoreDeEngine;
import io.dataease.datasource.server.DatasourceServer;
import io.dataease.extensions.datasource.dto.TableField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service("h2Engine")
public class H2EngineProvider extends EngineProvider {

    private static final String creatTableSql =
            "CREATE TABLE IF NOT EXISTS TABLE_NAME" +
                    "Column_Fields;";


    @Override
    public String createView(String name, String viewSQL) {
        return "CREATE or replace view " + quoteIdentifier(name, '"') + " AS (" + viewSQL + ")";
    }

    @Override
    public String insertSql(String dsType, String tableName, DatasourceServer.UpdateType extractType, List<String[]> dataList, int page, int pageNumber, List<TableField> tableFields, CoreDeEngine engine) {
        String engineTableName;
        switch (extractType) {
            case all_scope:
                engineTableName = TableUtils.tmpName(TableUtils.tableName(tableName));
                break;
            default:
                engineTableName = TableUtils.tableName(tableName);
                break;
        }
        String insertSql = "INSERT INTO TABLE_NAME VALUES ".replace("TABLE_NAME", quoteIdentifier(engineTableName, '"'));
        StringBuffer values = new StringBuffer();

        Integer realSize = page * pageNumber < dataList.size() ? page * pageNumber : dataList.size();
        for (String[] strings : dataList.subList((page - 1) * pageNumber, realSize)) {
            int length = 0;
            String[] strings1 = new String[tableFields.stream().filter(TableField::isChecked).toList().size()];
            for (int i = 0; i < strings.length; i++) {
                if (tableFields.get(i).isChecked()) {
                    if (StringUtils.isEmpty(strings[i])) {
                        String type = tableFields.get(i).getType() == null ? tableFields.get(i).getFieldType() : tableFields.get(i).getType();
                        strings1[length] = null;
                    } else {
                        strings1[length] = strings[i].replace("\\", "\\\\").replace("'", "''");
                    }
                    length++;
                }
            }
            values.append("('").append(String.join("','", Arrays.asList(strings1)))
                    .append("'),");
        }
        return (insertSql + values.substring(0, values.length() - 1)).replaceAll("'null'", "null");
    }


    @Override
    public String dropTable(String name, CoreDeEngine engine) {
        return "DROP TABLE IF EXISTS " + quoteIdentifier(name, '"');
    }

    @Override
    public boolean needCheckExistTable() {
        return false;
    }

    @Override
    public String dropView(String name) {
        return "DROP VIEW IF EXISTS " + quoteIdentifier(name, '"');
    }

    @Override
    public String replaceTable(String name, CoreDeEngine engine) {
        String table = quoteIdentifier(name, '"');
        String tmpTable = quoteIdentifier(TableUtils.tmpName(name), '"');
        String oldTable = quoteIdentifier(name + "_tmp", '"');
        String replaceTableSql = "ALTER TABLE " + table + " RENAME TO " + oldTable + "; ALTER TABLE " + tmpTable
                + " RENAME TO " + table + "; ALTER TABLE " + oldTable + " RENAME TO " + tmpTable;
        return replaceTableSql + "; DROP TABLE IF EXISTS " + tmpTable + ";";
    }


    @Override
    public String createTableSql(String tableName, List<TableField> tableFields, CoreDeEngine engine) {
        String quotedTable = quoteIdentifier(tableName, '"');
        String columnSql = createTableSql(tableFields);
        return creatTableSql.replace("TABLE_NAME", quotedTable).replace("Column_Fields", columnSql);
    }

    private String createTableSql(final List<TableField> tableFields) {
        List<String> columns = new ArrayList<>();
        List<String> keys = new ArrayList<>();
        for (TableField tableField : tableFields) {
            if (!tableField.isChecked()) {
                continue;
            }
            if (tableField.isPrimaryKey()) {
                keys.add(quoteIdentifier(tableField.getName(), '"'));
            }
            columns.add(quoteIdentifier(tableField.getName(), '"') + " " + buildColumnType(tableField));
        }
        StringBuilder sql = new StringBuilder("(").append(String.join(",", columns));
        if (!keys.isEmpty()) {
            sql.append(", PRIMARY KEY (").append(String.join(",", keys)).append(")");
        }
        sql.append(")");
        return sql.toString();
    }

    private String buildColumnType(TableField tableField) {
        switch (tableField.getDeExtractType()) {
            case 0:
                if (StringUtils.isNotEmpty(tableField.getLength())) {
                    return "varchar(" + tableField.getLength() + ")";
                }
                return "longtext";
            case 1:
                return "varchar(2048)";
            case 2:
                return "bigint(20)";
            case 3:
                return "decimal(27,8)";
            case 4:
                return "TINYINT(" + tableField.getPrecision() + ")";
            default:
                return "longtext";
        }
    }
}
