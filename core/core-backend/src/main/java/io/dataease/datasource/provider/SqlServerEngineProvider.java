package io.dataease.datasource.provider;


import io.dataease.dataset.utils.TableUtils;
import io.dataease.datasource.dao.auto.entity.CoreDeEngine;
import io.dataease.datasource.server.DatasourceServer;
import io.dataease.datasource.type.Sqlserver;
import io.dataease.extensions.datasource.dto.TableField;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import io.dataease.utils.JsonUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author gin
 * @Date 2021/5/17 4:27 下午
 */
@Service("sqlServerEngine")
public class SqlServerEngineProvider extends EngineProvider {

    private static final String creatTableSql =
            "CREATE TABLE TABLE_NAME" +
                    "Column_Fields;";


    @Override
    public String createView(String name, String viewSQL) {
        return "CREATE or replace view " + bracketIdentifier(name) + " AS (" + viewSQL + ")";
    }

    @Override
    public String insertSql(String dsType, String tableName, DatasourceServer.UpdateType extractType, List<String[]> dataList, int page, int pageNumber, List<TableField> tableFields, CoreDeEngine engine) {
        Sqlserver sqlserver = JsonUtil.parseObject(engine.getConfiguration(), Sqlserver.class);
        String engineTableName;
        switch (extractType) {
            case all_scope:
                engineTableName = TableUtils.tmpName(TableUtils.tableName(tableName));
                break;
            default:
                engineTableName = TableUtils.tableName(tableName);
                break;
        }

        String insertSql = "INSERT INTO TABLE_NAME VALUES ".replace("TABLE_NAME", bracketIdentifier(engineTableName));
        StringBuffer values = new StringBuffer();

        Integer realSize = page * pageNumber < dataList.size() ? page * pageNumber : dataList.size();
        for (String[] strings : dataList.subList((page - 1) * pageNumber, realSize)) {
            List<String> rowValues = new ArrayList<>();
            for (int i = 0; i < strings.length; i++) {
                TableField tableField = tableFields.get(i);
                if (!tableField.isChecked()) {
                    continue;
                }
                if (StringUtils.isEmpty(strings[i])) {
                    String type = tableField.getType() == null ? tableField.getFieldType() : tableField.getType();
                    if (type.equals("LONG") || type.equals("DOUBLE")) {
                        rowValues.add("0");
                    } else {
                        rowValues.add("null");
                    }
                } else {
                    String escaped = strings[i].replace("\\", "\\\\").replace("'", "\\'");
                    Integer deExtractType = tableField.getDeExtractType();
                    if (Integer.valueOf(0).equals(deExtractType)) {
                        rowValues.add("N'" + escaped + "'");
                    } else if (Integer.valueOf(1).equals(deExtractType)) {
                        rowValues.add("'" + escaped + "'");
                    } else {
                        rowValues.add(escaped);
                    }
                }
            }
            values.append("(").append(String.join(",", rowValues)).append("),");
        }
        String insetSql = insertSql + values.substring(0, values.length() - 1);
        if (dsType.contains(DatasourceConfiguration.DatasourceType.API.name())) {
            List<TableField> keys = tableFields.stream().filter(tableField -> tableField.isPrimaryKey() && tableField.isChecked()).toList();
            List<TableField> notKeys = tableFields.stream().filter(tableField -> tableField.isChecked() && !tableField.isPrimaryKey()).toList();
            if (CollectionUtils.isNotEmpty(keys) && extractType.equals(DatasourceServer.UpdateType.add_scope)) {
                String keyColumns = keys.stream().map(f -> bracketIdentifier(f.getName())).collect(Collectors.joining(","));
                insetSql = insetSql + " ON CONFLICT (" + keyColumns + ") DO UPDATE SET ";
                List<String> updateColumes = new ArrayList<>();
                for (TableField notKey : notKeys) {
                    updateColumes.add(bracketIdentifier(notKey.getName()) + " = EXCLUDED." + bracketIdentifier(notKey.getName()));
                }
                insetSql = insetSql + updateColumes.stream().collect(Collectors.joining(","));
            }
        }

        return insetSql;
    }


    @Override
    public String dropTable(String name, CoreDeEngine engine) {
        return "DROP TABLE " + bracketIdentifier(name);
    }

    @Override
    public boolean needCheckExistTable() {
        return true;
    }

    @Override
    public String dropView(String name) {
        return "DROP VIEW " + bracketIdentifier(name);
    }

    @Override
    public String replaceTable(String name, CoreDeEngine engine) {
        String table = bracketIdentifier(name);
        String tmpTable = bracketIdentifier(TableUtils.tmpName(name));
        String oldTable = bracketIdentifier(name + "_tmp");
        String replaceTableSql = "EXEC sp_rename " + table + ", " + oldTable + ", 'OBJECT'; EXEC sp_rename " + tmpTable
                + ", " + table + ", 'OBJECT'; EXEC sp_rename " + oldTable + ", " + tmpTable + ", 'OBJECT'";
        String dropTableSql = "DROP TABLE " + tmpTable;
        return replaceTableSql + ";" + dropTableSql;
    }

    @Override
    public String createTableSql(String tableName, List<TableField> tableFields, CoreDeEngine engine) {
        String columnSql = createTableSql(tableFields);
        return creatTableSql.replace("TABLE_NAME", bracketIdentifier(tableName)).replace("Column_Fields", columnSql);
    }

    private String createTableSql(final List<TableField> tableFields) {
        List<String> columns = new ArrayList<>();
        List<String> keys = new ArrayList<>();
        for (TableField tableField : tableFields) {
            if (!tableField.isChecked()) {
                continue;
            }
            if (tableField.isPrimaryKey()) {
                keys.add(bracketIdentifier(tableField.getName()));
            }
            columns.add(bracketIdentifier(tableField.getName()) + " " + buildColumnType(tableField));
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
                    return "nvarchar(" + tableField.getLength() + ")";
                }
                return "nvarchar(max)";
            case 1:
                return "DATETIME";
            case 2:
                return "bigint";
            case 3:
                return "DECIMAL(27,8)";
            case 4:
                return "TINYINT";
            default:
                return "nvarchar(max)";
        }
    }

    private String bracketIdentifier(String name) {
        validateIdentifier(name);
        return "[" + name + "]";
    }
}
