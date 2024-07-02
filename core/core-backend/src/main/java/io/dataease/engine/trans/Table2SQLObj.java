package io.dataease.engine.trans;

import io.dataease.engine.constant.SQLConstants;
import io.dataease.extensions.datasource.constant.SqlPlaceholderConstants;
import io.dataease.extensions.datasource.model.SQLMeta;
import io.dataease.extensions.datasource.model.SQLObj;

/**
 * @Author Junjun
 */
public class Table2SQLObj {
    public static void table2sqlobj(SQLMeta meta, String tablePrefix, String table, boolean crossDs) {
        String sql;
        if (table.startsWith("(") && table.endsWith(")") && !crossDs) {// SQL片段和关联
            meta.setTableDialect(table.substring(1, table.length() - 1));
            sql = "(" + SqlPlaceholderConstants.TABLE_PLACEHOLDER + ")";
        } else {
            sql = table;
        }
        SQLObj tableObj = SQLObj.builder()
                .tableName((table.startsWith("(") && table.endsWith(")")) ? sql : String.format(SQLConstants.TABLE_NAME, tablePrefix, table))
                .tableAlias(String.format(SQLConstants.TABLE_ALIAS_PREFIX, 0))
                .build();
        meta.setTable(tableObj);
    }
}
