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
        return "CREATE or replace view " + name + " AS (" + viewSQL + ")";
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

        String insertSql = "INSERT INTO TABLE_NAME VALUES ".replace("TABLE_NAME", sqlserver.getSchema() + "." + engineTableName);
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
                insetSql = insetSql + " ON CONFLICT (key) DO UPDATE SET ".replace("key", keys.stream().map(TableField::getName).collect(Collectors.joining(",")));
                List<String> updateColumes = new ArrayList<>();
                for (TableField notKey : notKeys) {
                    updateColumes.add("column = EXCLUDED.column".replace("column", notKey.getName()));
                }
                insetSql = insetSql + updateColumes.stream().collect(Collectors.joining(","));
            }
        }

        return insetSql;
    }


    @Override
    public String dropTable(String name, CoreDeEngine engine) {
        Sqlserver sqlserver = JsonUtil.parseObject(engine.getConfiguration(), Sqlserver.class);
        return "DROP TABLE " + sqlserver.getSchema() + "." + name + "";
    }

    @Override
    public boolean needCheckExistTable() {
        return true;
    }

    @Override
    public String dropView(String name) {
        return "DROP VIEW " + name + "";
    }

    @Override
    public String replaceTable(String name, CoreDeEngine engine) {
        Sqlserver sqlserver = JsonUtil.parseObject(engine.getConfiguration(), Sqlserver.class);
        String replaceTableSql = " EXEC sp_rename 'SCHEMA.FROM_TABLE', 'FROM_TABLE_tmp', 'OBJECT'; EXEC sp_rename 'SCHEMA.TO_TABLE', 'FROM_TABLE', 'OBJECT'; EXEC sp_rename 'SCHEMA.FROM_TABLE_tmp', 'TO_TABLE', 'OBJECT' "
                .replace("FROM_TABLE", name).replace("TO_TABLE", TableUtils.tmpName(name)).replace("SCHEMA", sqlserver.getSchema());
        String dropTableSql = "DROP TABLE TABLE_NAME".replace("TABLE_NAME", sqlserver.getSchema() + "." + TableUtils.tmpName(name));
        return replaceTableSql + ";" + dropTableSql;
    }

    @Override
    public String createTableSql(String tableName, List<TableField> tableFields, CoreDeEngine engine) {
        String dorisTableColumnSql = createTableSql(tableFields);
        Sqlserver sqlserver = JsonUtil.parseObject(engine.getConfiguration(), Sqlserver.class);
        return creatTableSql.replace("TABLE_NAME", sqlserver.getSchema() + "." + tableName).replace("Column_Fields", dorisTableColumnSql);
    }

    private String createTableSql(final List<TableField> tableFields) {
        StringBuilder columnFields = new StringBuilder("");
        StringBuilder key = new StringBuilder();
        for (TableField tableField : tableFields) {
            if (!tableField.isChecked()) {
                continue;
            }
            if (tableField.isPrimaryKey()) {
                key.append("").append(tableField.getName()).append(", ");
            }
            columnFields.append(tableField.getName()).append(" ");
            int size = tableField.getPrecision() * 4;
            switch (tableField.getDeExtractType()) {
                case 0:
                    if (StringUtils.isNotEmpty(tableField.getLength())) {
                        columnFields.append("varchar(length)".replace("length", tableField.getLength())).append(",");
                    } else {
                        columnFields.append("varchar(max)").append(",");
                    }
                    break;
                case 1:
                    columnFields.append("DATETIME").append(",");
                    break;
                case 2:
                    columnFields.append("bigint").append(",");
                    break;
                case 3:
                    columnFields.append("DECIMAL(27,8)").append(",");
                    break;
                case 4:
                    columnFields.append("TINYINT".replace("length", String.valueOf(tableField.getPrecision()))).append(",");
                    break;
                default:
                    columnFields.append("varchar(max)").append(",");
                    break;
            }
        }
        if (StringUtils.isEmpty(key.toString())) {
            columnFields = new StringBuilder(columnFields.substring(0, columnFields.length() - 1));
        } else {
            key = new StringBuilder(key.substring(0, key.length() - 2));
            columnFields.append(" PRIMARY KEY (PRIMARYKEY)".replace("PRIMARYKEY", key.toString()));
        }

        columnFields = new StringBuilder("(" + columnFields + ")");
        return columnFields.toString();
    }
}
