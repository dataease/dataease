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
        return "CREATE or replace view " + quoteIdent(name, '"') + " AS (" + viewSQL + ")";
    }

    @Override
    public String insertSql(String dsType, String tableName, DatasourceServer.UpdateType extractType, List<String[]> dataList, int page, int pageNumber, List<TableField> tableFields) {
        String engineTableName;
        switch (extractType) {
            case all_scope:
                engineTableName = TableUtils.tmpName(TableUtils.tableName(tableName));
                break;
            default:
                engineTableName = TableUtils.tableName(tableName);
                break;
        }
        String insertSql = "INSERT INTO TABLE_NAME VALUES ".replace("TABLE_NAME", quoteIdent(engineTableName, '"'));
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
    public String dropTable(String name) {
        return "DROP TABLE IF EXISTS " + quoteIdent(name, '`');
    }

    @Override
    public String dropView(String name) {
        return "DROP VIEW IF EXISTS " + quoteIdent(name, '`');
    }

    @Override
    public String replaceTable(String name) {
        String from = quoteIdent(name, '`');
        String fromTmp = quoteIdent(name + "_tmp", '`');
        String to = quoteIdent(TableUtils.tmpName(name), '`');
        return "ALTER TABLE " + from + " rename to " + fromTmp
                + "; ALTER TABLE " + to + " rename to " + from
                + "; DROP TABLE IF EXISTS " + fromTmp + ";";
    }

    @Override
    public String createTableSql(String tableName, List<TableField> tableFields, CoreDeEngine engine) {
        String quotedTableName = quoteIdent(tableName, '"');
        String dorisTableColumnSql = createTableSql(tableFields);
        return creatTableSql.replace("TABLE_NAME", quotedTableName).replace("Column_Fields", dorisTableColumnSql);
    }

    private String createTableSql(final List<TableField> tableFields) {
        List<String> columnDefs = new ArrayList<>();
        List<String> primaryKeys = new ArrayList<>();
        for (TableField tableField : tableFields) {
            if (!tableField.isChecked()) {
                continue;
            }
            String quotedName = quoteIdent(tableField.getName(), '"');
            if (tableField.isPrimaryKey()) {
                primaryKeys.add(quotedName);
            }
            String type;
            switch (tableField.getDeExtractType()) {
                case 0:
                    if (StringUtils.isNotEmpty(tableField.getLength())) {
                        type = "varchar(" + tableField.getLength() + ")";
                    } else {
                        type = "longtext";
                    }
                    break;
                case 1:
                    type = "varchar(2048)";
                    break;
                case 2:
                    type = "bigint(20)";
                    break;
                case 3:
                    type = "decimal(27,8)";
                    break;
                case 4:
                    type = "TINYINT(" + tableField.getPrecision() + ")";
                    break;
                default:
                    type = "longtext";
                    break;
            }
            columnDefs.add(quotedName + " " + type);
        }
        if (!primaryKeys.isEmpty()) {
            columnDefs.add("PRIMARY KEY (" + String.join(", ", primaryKeys) + ")");
        }
        return "(" + String.join(",", columnDefs) + ")";
    }
}
