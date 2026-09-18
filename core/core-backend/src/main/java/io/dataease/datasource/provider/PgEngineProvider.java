package io.dataease.datasource.provider;


import io.dataease.dataset.utils.TableUtils;
import io.dataease.datasource.dao.auto.entity.CoreDeEngine;
import io.dataease.datasource.server.DatasourceServer;
import io.dataease.extensions.datasource.dto.TableField;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
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
@Service("pgEngine")
public class PgEngineProvider extends EngineProvider {

    private static final String creatTableSql =
            "CREATE TABLE IF NOT EXISTS TABLE_NAME " +
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
                        if (type.equals("LONG") || type.equals("DOUBLE")) {
                            strings1[length] = "0";
                        } else {
                            strings1[length] = null;
                        }
                    } else {
                        strings1[length] = strings[i].replace("\\", "\\\\").replace("'", "\\'");
                    }
                    length++;
                }
            }
            values.append("('").append(String.join("','", Arrays.asList(strings1)))
                    .append("'),");
        }
        String insetSql = (insertSql + values.substring(0, values.length() - 1)).replaceAll("'null'", "null");
        if (dsType.contains(DatasourceConfiguration.DatasourceType.API.name())) {
            List<TableField> keys = tableFields.stream().filter(tableField -> tableField.isPrimaryKey() && tableField.isChecked()).toList();
            List<TableField> notKeys = tableFields.stream().filter(tableField -> tableField.isChecked() && !tableField.isPrimaryKey()).toList();
            if (CollectionUtils.isNotEmpty(keys) && extractType.equals(DatasourceServer.UpdateType.add_scope)) {
                String keyColumns = keys.stream().map(f -> quoteIdentifier(f.getName(), '"')).collect(Collectors.joining(","));
                insetSql = insetSql + " ON CONFLICT (" + keyColumns + ") DO UPDATE SET ";
                List<String> updateColumes = new ArrayList<>();
                for (TableField notKey : notKeys) {
                    updateColumes.add(quoteIdentifier(notKey.getName(), '"') + " = EXCLUDED." + quoteIdentifier(notKey.getName(), '"'));
                }
                insetSql = insetSql + updateColumes.stream().collect(Collectors.joining(","));
            }
        }

        return insetSql;
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
        String replaceTableSql = "ALTER TABLE " + table + " RENAME TO " + tmpTable + "; ALTER TABLE " + tmpTable
                + " RENAME TO " + table + "; ALTER TABLE " + tmpTable + " RENAME TO " + table;
        String dropTableSql = "DROP TABLE IF EXISTS " + tmpTable;
        return replaceTableSql + ";" + dropTableSql;
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
                return "text";
            case 1:
                return "timestamp";
            case 2:
                return "bigint";
            case 3:
                return "numeric(27,8)";
            case 4:
                return "BOOLEAN";
            default:
                return "text";
        }
    }
}
