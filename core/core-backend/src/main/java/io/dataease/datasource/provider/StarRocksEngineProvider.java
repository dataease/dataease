package io.dataease.datasource.provider;

import io.dataease.dataset.utils.TableUtils;
import io.dataease.datasource.dao.auto.entity.CoreDeEngine;
import io.dataease.datasource.server.DatasourceServer;
import io.dataease.datasource.type.StarRocks;
import io.dataease.extensions.datasource.dto.TableField;
import io.dataease.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * StarRocks 与 Doris 同源，SQL 语法高度兼容 MySQL。
 * 建表使用 StarRocks 类型体系，写入使用 Stream Load，表替换使用 ALTER TABLE ... RENAME。
 */
@Service("StarRocksEngine")
public class StarRocksEngineProvider extends MysqlEngineProvider {

    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS TABLE_NAME Column_FieldsKEY_CLAUSE DISTRIBUTED BY HASH(DISTRIBUTION_COLUMN) BUCKETS 10 PROPERTIES (\"replication_num\" = \"1\")";

    public void streamLoadInsert(String tableName, DatasourceServer.UpdateType extractType, List<String[]> dataList, List<TableField> tableFields, CoreDeEngine engine) {
        String engineTableName;
        if (DatasourceServer.UpdateType.all_scope.equals(extractType)) {
            engineTableName = TableUtils.tmpName(TableUtils.tableName(tableName));
        } else {
            engineTableName = TableUtils.tableName(tableName);
        }
        StarRocks configuration = JsonUtil.parseObject(engine.getConfiguration(), StarRocks.class);
        StarRocksStreamLoadUtil.streamLoad(configuration, engineTableName, dataList, tableFields);
    }

    @Override
    public String createTableSql(String tableName, List<TableField> tableFields, CoreDeEngine engine) {
        validateSqlInjectionRisk(tableName);
        List<TableField> checkedFields = tableFields.stream().filter(TableField::isChecked).toList();
        List<TableField> primaryKeyFields = checkedFields.stream()
                .filter(TableField::isPrimaryKey)
                .toList();
        TableField distributionField = primaryKeyFields.stream()
                .findFirst()
                .orElse(checkedFields.isEmpty() ? null : checkedFields.getFirst());
        String distributionColumn = distributionField == null ? "c" : distributionField.getName();
        String keyClause = primaryKeyFields.isEmpty() ? "" : " PRIMARY KEY (" + primaryKeyFields.stream()
                .map(tableField -> quoteIdentifier(tableField.getName(), '`'))
                .collect(Collectors.joining(", ")) + ")";
        return CREATE_TABLE_SQL
                .replace("TABLE_NAME", quoteIdentifier(tableName, '`'))
                .replace("Column_Fields", createTableFields(tableFields))
                .replace("KEY_CLAUSE", keyClause)
                .replace("DISTRIBUTION_COLUMN", quoteIdentifier(distributionColumn, '`'));
    }

    private String createTableFields(List<TableField> tableFields) {
        List<String> columnDefinitions = tableFields.stream()
                .filter(TableField::isChecked)
                .map(tableField -> quoteIdentifier(tableField.getName(), '`') + " " + buildColumnType(tableField))
                .toList();
        return "(" + String.join(", ", columnDefinitions) + ")";
    }

    private String buildColumnType(TableField tableField) {
        switch (tableField.getDeExtractType()) {
            case 0:
                if (StringUtils.isNotEmpty(tableField.getLength())) {
                    return "varchar(" + tableField.getLength() + ")";
                }
                return "string";
            case 1:
                return "datetime";
            case 2:
                return "bigint";
            case 3:
                return "decimal(27,8)";
            case 4:
                return "boolean";
            default:
                return "string";
        }
    }

    @Override
    public String replaceTable(String name, CoreDeEngine engine) {
        String table = quoteIdentifier(name, '`');
        String tmpTable = quoteIdentifier(TableUtils.tmpName(name), '`');
        String replaceTableSql = "ALTER TABLE " + table + " RENAME " + tmpTable + ";"
                + "ALTER TABLE " + tmpTable + " RENAME " + table + ";"
                + "ALTER TABLE " + tmpTable + " RENAME " + table;
        String dropTableSql = "DROP TABLE IF EXISTS " + tmpTable;
        return replaceTableSql + ";" + dropTableSql;
    }
}
