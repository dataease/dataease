package io.dataease.engine.sql;

import io.dataease.api.chart.dto.ChartViewDTO;
import io.dataease.api.dataset.union.model.SQLMeta;
import io.dataease.api.dataset.union.model.SQLObj;
import io.dataease.engine.constant.SQLConstants;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupString;

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
    public static String createQuerySQLAsTmp(SQLMeta sqlMeta, boolean isGroup, boolean needOrder) {
        return createQuerySQL(sqlMeta, isGroup, needOrder);
    }

    public static String createQuerySQLWithLimit(SQLMeta sqlMeta, boolean isGroup, boolean needOrder, int start, int count) {
        return createQuerySQL(sqlMeta, isGroup, needOrder) + " LIMIT " + count + " OFFSET " + start;
    }

    public static String createQuerySQL(SQLMeta sqlMeta, boolean isGroup, boolean needOrder) {
        List<SQLObj> xFields = sqlMeta.getXFields();
        SQLObj tableObj = sqlMeta.getTable();
        List<SQLObj> xOrders = sqlMeta.getXOrders();

        STGroup stg = new STGroupString(SqlTemplate.PREVIEW_SQL);
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

        // check datasource 是否需要排序
        if (needOrder && ObjectUtils.isEmpty(xOrders)) {
            if (ObjectUtils.isNotEmpty(xFields)) {
                xOrders = new ArrayList<>();
                SQLObj sqlObj = xFields.get(0);
                SQLObj result = SQLObj.builder().orderField(sqlObj.getFieldAlias()).orderAlias(sqlObj.getFieldAlias()).orderDirection("ASC").build();
                xOrders.add(result);
            }
        }
        if (ObjectUtils.isNotEmpty(xOrders)) {
            st_sql.add("orders", xOrders);
        }

        return st_sql.render();
    }

    public static String createQuerySQL(SQLMeta sqlMeta, boolean isGroup, boolean needOrder, ChartViewDTO view) {
        STGroup stg = new STGroupString(SqlTemplate.PREVIEW_SQL);
        ST st_sql = stg.getInstanceOf("previewSql");

        st_sql.add("isGroup", isGroup);

        SQLObj tableObj = sqlMeta.getTable();
        if (ObjectUtils.isNotEmpty(tableObj)) st_sql.add("table", tableObj);

        List<SQLObj> xFields = sqlMeta.getXFields();
        List<SQLObj> xOrders = sqlMeta.getXOrders();
        if (ObjectUtils.isNotEmpty(xFields)) st_sql.add("groups", xFields);

        List<SQLObj> yFields = sqlMeta.getYFields();
        List<String> yWheres = sqlMeta.getYWheres();
        List<SQLObj> yOrders = sqlMeta.getYOrders();
        if (ObjectUtils.isNotEmpty(yFields)) st_sql.add("aggregators", yFields);

        String customWheres = sqlMeta.getCustomWheres();
        String extWheres = sqlMeta.getExtWheres();
        String whereTrees = sqlMeta.getWhereTrees();
        List<String> wheres = new ArrayList<>();
        if (customWheres != null) wheres.add(customWheres);
        if (extWheres != null) wheres.add(extWheres);
        if (whereTrees != null) wheres.add(whereTrees);
        if (ObjectUtils.isNotEmpty(wheres)) st_sql.add("filters", wheres);
        String sql = st_sql.render();

        ST st = stg.getInstanceOf("previewSql");
        st_sql.add("isGroup", isGroup);

        SQLObj tableSQL = SQLObj.builder()
                .tableName(String.format(SQLConstants.BRACKETS, sql))
                .tableAlias(String.format(SQLConstants.TABLE_ALIAS_PREFIX, 1))
                .build();
        if (ObjectUtils.isNotEmpty(tableSQL)) st.add("table", tableSQL);

        List<String> aggWheres = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(yWheres)) aggWheres.addAll(yWheres);
        if (ObjectUtils.isNotEmpty(aggWheres)) st.add("filters", aggWheres);

        List<SQLObj> orders = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(xOrders)) orders.addAll(xOrders);
        if (ObjectUtils.isNotEmpty(yOrders)) orders.addAll(yOrders);
        // check datasource 是否需要排序
        if (needOrder && ObjectUtils.isEmpty(orders)) {
            if (ObjectUtils.isNotEmpty(xFields) || ObjectUtils.isNotEmpty(yFields)) {
                SQLObj sqlObj = ObjectUtils.isNotEmpty(xFields) ? xFields.get(0) : yFields.get(0);
                SQLObj result = SQLObj.builder().orderField(sqlObj.getFieldAlias()).orderAlias(sqlObj.getFieldAlias()).orderDirection("ASC").build();
                orders.add(result);
            }
        }
        if (ObjectUtils.isNotEmpty(orders)) st.add("orders", orders);

        return sqlLimit(st.render(), view);
    }

    public static String sqlLimit(String sql, ChartViewDTO view) {
        if (StringUtils.equalsIgnoreCase(view.getType(), "table-info")) {
            return sql;
        }
        if (StringUtils.equalsIgnoreCase(view.getResultMode(), "custom")) {
            return sql + " LIMIT " + view.getResultCount() + " OFFSET 0";
        } else {
            return sql;
        }
    }
}
