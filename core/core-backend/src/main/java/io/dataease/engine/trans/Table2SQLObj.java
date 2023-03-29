package io.dataease.engine.trans;

import io.dataease.engine.constant.SQLConstants;
import io.dataease.engine.model.SQLMeta;
import io.dataease.engine.model.SQLObj;

/**
 * @Author Junjun
 */
public class Table2SQLObj {
    public static void table2sqlobj(SQLMeta meta, String tablePrefix, String table) {
        SQLObj tableObj = SQLObj.builder()
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(SQLConstants.TABLE_NAME, tablePrefix, table))
                .tableAlias(String.format(SQLConstants.TABLE_ALIAS_PREFIX, 0))
                .build();
        meta.setTable(tableObj);
    }
}
