package io.dataease.engine.sql;

import io.dataease.api.dataset.union.model.SQLMeta;
import io.dataease.api.dataset.union.model.SQLObj;
import io.dataease.engine.constant.SQLConstants;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.util.CollectionUtils;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Junjun
 * <p>
 * 将SQLMeta各个部分先构建完毕，然后在这个类中拼接
 */
public class SQLProvider {

    /**
     * @param sqlMeta sql作为table，首尾用'(',')'
     * @param isGroup 是否聚合
     * @return
     */
    public static String createQuerySQLAsTmp(SQLMeta sqlMeta, boolean isGroup) {
        return createQuerySQL(sqlMeta, isGroup);
    }

    public static String createQuerySQL(SQLMeta sqlMeta, boolean isGroup) {
        List<SQLObj> xFields = sqlMeta.getXFields();
        SQLObj tableObj = sqlMeta.getTable();
        List<SQLObj> xOrders = sqlMeta.getXOrders();

        STGroup stg = new STGroupFile(SQLConstants.SQL_TEMPLATE);
        ST st_sql = stg.getInstanceOf("previewSql");
        st_sql.add("isGroup", isGroup);
        if (ObjectUtils.isNotEmpty(xFields)) st_sql.add("groups", xFields);
        if (ObjectUtils.isNotEmpty(tableObj)) st_sql.add("table", tableObj);
        String customWheres = sqlMeta.getCustomWheres();
        String whereTrees = sqlMeta.getWhereTrees();
        List<String> wheres = new ArrayList<>();
        if (customWheres != null) wheres.add(customWheres);
        if (whereTrees != null) wheres.add(whereTrees);
        if (ObjectUtils.isNotEmpty(wheres)) st_sql.add("filters", wheres);

        if (ObjectUtils.isNotEmpty(xOrders)) {
            st_sql.add("orders", xOrders);
        }

        return st_sql.render();
    }
}
