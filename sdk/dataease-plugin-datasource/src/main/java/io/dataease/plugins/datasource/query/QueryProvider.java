package io.dataease.plugins.datasource.query;

import com.google.gson.Gson;
import io.dataease.plugins.common.base.domain.ChartViewWithBLOBs;
import io.dataease.plugins.common.base.domain.DatasetTableField;
import io.dataease.plugins.common.base.domain.Datasource;
import io.dataease.plugins.common.constants.datasource.PgConstants;
import io.dataease.plugins.common.dto.chart.ChartViewFieldDTO;
import io.dataease.plugins.common.dto.dataset.SqlVariableDetails;
import io.dataease.plugins.common.dto.datasource.DeSortField;
import io.dataease.plugins.common.dto.sqlObj.SQLObj;
import io.dataease.plugins.common.request.chart.ChartExtFilterRequest;
import io.dataease.plugins.common.request.chart.filter.FilterTreeItem;
import io.dataease.plugins.common.request.chart.filter.FilterTreeObj;
import io.dataease.plugins.common.request.permission.DataSetRowPermissionsTreeDTO;
import io.dataease.plugins.common.request.permission.DatasetRowPermissionsTreeItem;
import io.dataease.plugins.common.request.permission.DatasetRowPermissionsTreeObj;
import io.dataease.plugins.datasource.entity.Dateformat;
import io.dataease.plugins.datasource.entity.JdbcConfiguration;
import io.dataease.plugins.datasource.entity.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author gin
 * @Date 2021/5/17 2:42 下午
 */
public abstract class QueryProvider {
    public abstract Integer transFieldType(String field);

    public abstract String createSQLPreview(String sql, String orderBy);

    public abstract String createQuerySQL(String table, List<DatasetTableField> fields, boolean isGroup, Datasource ds, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree);

    public abstract String createQuerySQLAsTmp(String sql, List<DatasetTableField> fields, boolean isGroup, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree);

    public abstract String createQuerySQL(String table, List<DatasetTableField> fields, boolean isGroup, Datasource ds, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<DeSortField> sortFields, Long limit, String keyword);

    public abstract String createQuerySQLAsTmp(String sql, List<DatasetTableField> fields, boolean isGroup, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<DeSortField> sortFields, Long limit, String keyword);

    public abstract String createQueryTableWithPage(String table, List<DatasetTableField> fields, Integer page, Integer pageSize, Integer realSize, boolean isGroup, Datasource ds, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree);

    public abstract String createQuerySQLWithPage(String sql, List<DatasetTableField> fields, Integer page, Integer pageSize, Integer realSize, boolean isGroup, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree);

    public abstract String createQueryTableWithLimit(String table, List<DatasetTableField> fields, Integer limit, boolean isGroup, Datasource ds, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree);

    public abstract String createQuerySqlWithLimit(String sql, List<DatasetTableField> fields, Integer limit, boolean isGroup, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree);

    public abstract String getSQL(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds, ChartViewWithBLOBs view);

    public abstract String getSQLAsTmp(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, ChartViewWithBLOBs view);

    public abstract String getSQLTableInfo(String table, List<ChartViewFieldDTO> xAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds, ChartViewWithBLOBs view);

    public abstract String getSQLAsTmpTableInfo(String sql, List<ChartViewFieldDTO> xAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds, ChartViewWithBLOBs view);

    public String getSQLWithPage(boolean isTable, String sql, List<ChartViewFieldDTO> xAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds, ChartViewWithBLOBs view, PageInfo pageInfo) {
        if (isTable) {
            return getSQLTableInfo(sql, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, ds, view);
        } else {
            return getSQLAsTmpTableInfo(sql, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, ds, view);
        }
    }

    public String getResultCount(boolean isTable, String sql, List<ChartViewFieldDTO> xAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds, ChartViewWithBLOBs view) {
        if (isTable) {
            return "SELECT COUNT(*) from (" + getSQLTableInfo(sql, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, ds, view) + ") COUNT_TEMP";
        } else {
            return "SELECT COUNT(*) from (" + getSQLAsTmpTableInfo(sql, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, ds, view) + ") COUNT_TEMP";
        }
    }

    public abstract String getSQLStack(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extStack, Datasource ds, ChartViewWithBLOBs view);

    public abstract String getSQLAsTmpStack(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extStack, ChartViewWithBLOBs view);

    public abstract String getSQLScatter(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extBubble, List<ChartViewFieldDTO> extGroup, Datasource ds, ChartViewWithBLOBs view);

    public abstract String getSQLAsTmpScatter(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extBubble, List<ChartViewFieldDTO> extGroup, ChartViewWithBLOBs view);

    public abstract String getSQLRangeBar(String table, List<ChartViewFieldDTO> baseXAxis, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extStack, Datasource ds, ChartViewWithBLOBs view);

    public abstract String getSQLAsTmpRangeBar(String table, List<ChartViewFieldDTO> baseXAxis, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extStack, ChartViewWithBLOBs view);

    public abstract String searchTable(String table);

    public abstract String getSQLSummary(String table, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, ChartViewWithBLOBs view, Datasource ds);

    public Integer transFieldSize(String type) {
        return 50;
    }

    public abstract String getSQLSummaryAsTmp(String sql, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, ChartViewWithBLOBs view);

    public abstract String wrapSql(String sql);

    public abstract String createRawQuerySQL(String table, List<DatasetTableField> fields, Datasource ds);

    public abstract String createRawQuerySQLAsTmp(String sql, List<DatasetTableField> fields);

    public abstract String transTreeItem(SQLObj tableObj, DatasetRowPermissionsTreeItem item);

    public abstract String transTreeItem(SQLObj tableObj, FilterTreeItem item);

    public void setSchema(SQLObj tableObj, Datasource ds) {
        if (ds != null && !tableObj.getTableName().startsWith("(") && !tableObj.getTableName().endsWith(")")) {
            JdbcConfiguration configuration = new Gson().fromJson(ds.getConfiguration(), JdbcConfiguration.class);
            String schema;
            if (ObjectUtils.isNotEmpty(configuration) && StringUtils.isNotBlank((schema = configuration.getSchema()))) {
                schema = String.format(PgConstants.KEYWORD_TABLE, schema);
                tableObj.setTableName(schema + "." + tableObj.getTableName());
            }
        }
    }

    public String convertTableToSql(String tableName, Datasource ds) {
        return "select * from  TABLE_NAME".replace("TABLE_NAME", tableName);
    }

    public String getLogic(String logic) {
        if (logic != null) {
            switch (logic) {
                case "and":
                    return "AND";
                case "or":
                    return "OR";
            }
        }
        return "AND";
    }

    public String transFilter(ChartExtFilterRequest chartExtFilterRequest, SqlVariableDetails sqlVariableDetails) {
        if (chartExtFilterRequest.getOperator().equals("in")) {
            return "('" + String.join("','", chartExtFilterRequest.getValue()) + "')";
        } else if (chartExtFilterRequest.getOperator().equals("between")) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(sqlVariableDetails.getType().size() > 1 ? sqlVariableDetails.getType().get(1) : "YYYY");
            return simpleDateFormat.format(new Date(Long.parseLong(chartExtFilterRequest.getValue().get(0))));
        } else {
            return chartExtFilterRequest.getValue().get(0);
        }
    }

    // 解析tree
    public String transFilterTrees(SQLObj tableObj, List<DataSetRowPermissionsTreeDTO> requestList) {
        if (CollectionUtils.isEmpty(requestList)) {
            return null;
        }
        List<String> res = new ArrayList<>();
        // permission trees
        // 解析每个tree，然后多个tree之间用and拼接
        // 每个tree，如果是sub tree节点，则使用递归合并成一组条件
        for (DataSetRowPermissionsTreeDTO request : requestList) {
            DatasetRowPermissionsTreeObj tree = request.getTree();
            if (ObjectUtils.isEmpty(tree)) {
                continue;
            }
            String treeExp = transTreeToWhere(tableObj, tree);
            if (StringUtils.isNotEmpty(treeExp)) {
                res.add(treeExp);
            }
        }
        return CollectionUtils.isNotEmpty(res) ? "(" + String.join(" AND ", res) + ")" : null;
    }

    public String transChartFilterTrees(SQLObj tableObj, FilterTreeObj tree) {
        List<String> res = new ArrayList<>();
        // permission trees
        // 解析每个tree，然后多个tree之间用and拼接
        // 每个tree，如果是sub tree节点，则使用递归合并成一组条件
        if (ObjectUtils.isEmpty(tree)) {
            return null;
        }
        String treeExp = transTreeToWhere(tableObj, tree);
        if (StringUtils.isNotEmpty(treeExp)) {
            res.add(treeExp);
        }
        return CollectionUtils.isNotEmpty(res) ? "(" + String.join(" AND ", res) + ")" : null;
    }

    public String transKeywordFilterList(SQLObj tableObj, List<SQLObj> xFields, String keyword) {
        String tableAlias = tableObj.getTableAlias();
        return xFields.stream().map(xField -> {
            String whereName = xField.getFieldOriginName();
            return whereName + " LIKE '%" + keyword + "%'";
        }).collect(Collectors.joining(" or "));
    }

    private String transTreeToWhere(SQLObj tableObj, DatasetRowPermissionsTreeObj tree) {
        if (ObjectUtils.isEmpty(tree)) {
            return null;
        }
        String logic = tree.getLogic();
        List<DatasetRowPermissionsTreeItem> items = tree.getItems();
        List<String> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(items)) {
            // type is item or tree
            for (DatasetRowPermissionsTreeItem item : items) {
                String exp = null;
                if (StringUtils.equalsIgnoreCase(item.getType(), "item")) {
                    // 单个item拼接SQL，最后根据logic汇总
                    exp = transTreeItem(tableObj, item);
                } else if (StringUtils.equalsIgnoreCase(item.getType(), "tree")) {
                    // 递归tree
                    exp = transTreeToWhere(tableObj, item.getSubTree());
                }
                if (StringUtils.isNotEmpty(exp)) {
                    list.add(exp);
                }
            }
        }
        return CollectionUtils.isNotEmpty(list) ? "(" + String.join(" " + logic + " ", list) + ")" : null;
    }

    private String transTreeToWhere(SQLObj tableObj, FilterTreeObj tree) {
        if (ObjectUtils.isEmpty(tree)) {
            return null;
        }
        String logic = tree.getLogic();
        List<FilterTreeItem> items = tree.getItems();
        List<String> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(items)) {
            // type is item or tree
            for (FilterTreeItem item : items) {
                String exp = null;
                if (StringUtils.equalsIgnoreCase(item.getType(), "item")) {
                    // 单个item拼接SQL，最后根据logic汇总
                    exp = transTreeItem(tableObj, item);
                } else if (StringUtils.equalsIgnoreCase(item.getType(), "tree")) {
                    // 递归tree
                    exp = transTreeToWhere(tableObj, item.getSubTree());
                }
                if (StringUtils.isNotEmpty(exp)) {
                    list.add(exp);
                }
            }
        }
        return CollectionUtils.isNotEmpty(list) ? "(" + String.join(" " + logic + " ", list) + ")" : null;
    }

    public String sqlForPreview(String table, Datasource ds) {
        return "SELECT * FROM " + table;
    }

    public List<Dateformat> dateformat() {
        return new ArrayList<>();
    }

    public String getTotalCount(boolean isTable, String sql, Datasource ds) {
        return null;
    }

    public String formatLikeValue(String value) {
        if (StringUtils.isBlank(value)) return null;
        if (StringUtils.equals("%", value)) {
            return "'%\\\\" + value + "%'";
        }
        return "'%" + value + "%'";
    }
}
