package io.dataease.provider.query.doris;

import io.dataease.plugins.common.base.domain.ChartViewWithBLOBs;
import io.dataease.plugins.common.base.domain.DatasetTableField;
import io.dataease.plugins.common.base.domain.Datasource;
import io.dataease.plugins.common.constants.datasource.MySQLConstants;
import io.dataease.plugins.common.dto.chart.ChartFieldCustomFilterDTO;
import io.dataease.plugins.common.dto.chart.ChartViewFieldDTO;
import io.dataease.plugins.common.dto.datasource.DeSortField;
import io.dataease.plugins.common.dto.sqlObj.SQLObj;
import io.dataease.plugins.common.request.chart.ChartExtFilterRequest;
import io.dataease.plugins.common.request.chart.filter.FilterTreeItem;
import io.dataease.plugins.common.request.chart.filter.FilterTreeObj;
import io.dataease.plugins.common.request.permission.DataSetRowPermissionsTreeDTO;
import io.dataease.plugins.common.request.permission.DatasetRowPermissionsTreeItem;
import io.dataease.plugins.datasource.entity.Dateformat;
import io.dataease.plugins.datasource.entity.PageInfo;
import io.dataease.provider.query.mysql.MysqlQueryProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;


@Service("dorisQueryProvider")
public class DorisQueryProvider extends MysqlQueryProvider {

    public String getSQLWithPage(boolean isTable, String sql, List<ChartViewFieldDTO> xAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds, ChartViewWithBLOBs view, PageInfo pageInfo) {
        String limit = ((pageInfo.getGoPage() != null && pageInfo.getPageSize() != null) ? " LIMIT " + (pageInfo.getGoPage() - 1) * pageInfo.getPageSize() + "," + pageInfo.getPageSize() : "");
        if (isTable) {
            return originalTableInfo(sql, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, ds, view, true) + limit;
        } else {
            return originalTableInfo("(" + sqlFix(sql) + ")", xAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, ds, view, true) + limit;
        }
    }

    @Override
    public String createRawQuerySQL(String table, List<DatasetTableField> fields, Datasource ds) {
        String[] array = fields.stream().map(f -> {
            StringBuilder stringBuilder = new StringBuilder();
            if (f.getDeExtractType() == 4) { // 处理 tinyint
                stringBuilder.append("concat(`").append(f.getOriginName()).append("`,'') AS ").append(f.getDataeaseName());
            } else if (f.getDeExtractType() == 1 && f.getType().equalsIgnoreCase("YEAR")) { // 处理 YEAR
                stringBuilder.append("").append(String.format(MySQLConstants.DATE_FORMAT, "CONCAT(" + f.getOriginName() + ",'-01-01')", MySQLConstants.DEFAULT_DATE_FORMAT)).append(" AS ").append(f.getDataeaseName());
            } else {
                stringBuilder.append("`").append(f.getOriginName()).append("` AS ").append(f.getDataeaseName());
            }
            return stringBuilder.toString();
        }).toArray(String[]::new);
        return MessageFormat.format("SELECT {0} FROM {1}  ", StringUtils.join(array, ","), table);
    }

    public String getTotalCount(boolean isTable, String sql, Datasource ds) {
        return null;
    }

    @Override
    public Integer transFieldType(String field) {
        return super.transFieldType(field);
    }

    @Override
    public String createSQLPreview(String sql, String orderBy) {
        return super.createSQLPreview(sql, orderBy);
    }

    @Override
    public String createQuerySQL(String table, List<DatasetTableField> fields, boolean isGroup, Datasource ds, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree) {
        return super.createQuerySQL(table, fields, isGroup, ds, fieldCustomFilter, rowPermissionsTree);
    }

    @Override
    public String createQuerySQLAsTmp(String sql, List<DatasetTableField> fields, boolean isGroup, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree) {
        return super.createQuerySQLAsTmp(sql, fields, isGroup, fieldCustomFilter, rowPermissionsTree);
    }

    @Override
    public String createQuerySQL(String table, List<DatasetTableField> fields, boolean isGroup, Datasource ds, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<DeSortField> sortFields, Long limit, String keyword) {
        return super.createQuerySQL(table, fields, isGroup, ds, fieldCustomFilter, rowPermissionsTree, sortFields, limit, keyword);
    }

    @Override
    public String createQuerySQLAsTmp(String sql, List<DatasetTableField> fields, boolean isGroup, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<DeSortField> sortFields, Long limit, String keyword) {
        return super.createQuerySQLAsTmp(sql, fields, isGroup, fieldCustomFilter, rowPermissionsTree, sortFields, limit, keyword);
    }

    @Override
    public String createQueryTableWithPage(String table, List<DatasetTableField> fields, Integer page, Integer pageSize, Integer realSize, boolean isGroup, Datasource ds, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree) {
        return super.createQueryTableWithPage(table, fields, page, pageSize, realSize, isGroup, ds, fieldCustomFilter, rowPermissionsTree);
    }

    @Override
    public String createQueryTableWithLimit(String table, List<DatasetTableField> fields, Integer limit, boolean isGroup, Datasource ds, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree) {
        return super.createQueryTableWithLimit(table, fields, limit, isGroup, ds, fieldCustomFilter, rowPermissionsTree);
    }

    @Override
    public String createQuerySqlWithLimit(String sql, List<DatasetTableField> fields, Integer limit, boolean isGroup, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree) {
        return super.createQuerySqlWithLimit(sql, fields, limit, isGroup, fieldCustomFilter, rowPermissionsTree);
    }

    @Override
    public String createQuerySQLWithPage(String sql, List<DatasetTableField> fields, Integer page, Integer pageSize, Integer realSize, boolean isGroup, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree) {
        return super.createQuerySQLWithPage(sql, fields, page, pageSize, realSize, isGroup, fieldCustomFilter, rowPermissionsTree);
    }

    @Override
    public String getSQL(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds, ChartViewWithBLOBs view) {
        return super.getSQL(table, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, ds, view);
    }

    @Override
    public String getSQLRangeBar(String table, List<ChartViewFieldDTO> baseXAxis, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extStack, Datasource ds, ChartViewWithBLOBs view) {
        return super.getSQLRangeBar(table, baseXAxis, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, extStack, ds, view);
    }

    @Override
    public String getSQLAsTmpRangeBar(String table, List<ChartViewFieldDTO> baseXAxis, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extStack, ChartViewWithBLOBs view) {
        return super.getSQLAsTmpRangeBar(table, baseXAxis, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, extStack, view);
    }

    @Override
    public String getSQLTableInfo(String table, List<ChartViewFieldDTO> xAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds, ChartViewWithBLOBs view) {
        return super.getSQLTableInfo(table, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, ds, view);
    }

    @Override
    public String originalTableInfo(String table, List<ChartViewFieldDTO> xAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds, ChartViewWithBLOBs view, boolean needOrder) {
        return super.originalTableInfo(table, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, ds, view, needOrder);
    }

    @Override
    public String getSQLAsTmpTableInfo(String sql, List<ChartViewFieldDTO> xAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds, ChartViewWithBLOBs view) {
        return super.getSQLAsTmpTableInfo(sql, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, ds, view);
    }

    @Override
    public String getSQLAsTmp(String sql, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, ChartViewWithBLOBs view) {
        return super.getSQLAsTmp(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, view);
    }

    @Override
    public String getSQLStack(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extStack, Datasource ds, ChartViewWithBLOBs view) {
        return super.getSQLStack(table, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, extStack, ds, view);
    }

    @Override
    public String getSQLAsTmpStack(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extStack, ChartViewWithBLOBs view) {
        return super.getSQLAsTmpStack(table, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, extStack, view);
    }

    @Override
    public String getSQLScatter(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extBubble, List<ChartViewFieldDTO> extGroup, Datasource ds, ChartViewWithBLOBs view) {
        return super.getSQLScatter(table, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, extBubble, extGroup, ds, view);
    }

    @Override
    public String getSQLAsTmpScatter(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extBubble, List<ChartViewFieldDTO> extGroup, ChartViewWithBLOBs view) {
        return super.getSQLAsTmpScatter(table, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, extBubble, extGroup, view);
    }

    @Override
    public String searchTable(String table) {
        return super.searchTable(table);
    }

    @Override
    public String getSQLSummary(String table, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, ChartViewWithBLOBs view, Datasource ds) {
        return super.getSQLSummary(table, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, view, ds);
    }

    @Override
    public String getSQLSummaryAsTmp(String sql, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, ChartViewWithBLOBs view) {
        return super.getSQLSummaryAsTmp(sql, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, view);
    }

    @Override
    public String wrapSql(String sql) {
        return super.wrapSql(sql);
    }

    @Override
    public String createRawQuerySQLAsTmp(String sql, List<DatasetTableField> fields) {
        return super.createRawQuerySQLAsTmp(sql, fields);
    }

    @Override
    public String transTreeItem(SQLObj tableObj, DatasetRowPermissionsTreeItem item) {
        return super.transTreeItem(tableObj, item);
    }

    @Override
    public String transTreeItem(SQLObj tableObj, FilterTreeItem item) {
        return super.transTreeItem(tableObj, item);
    }

    @Override
    public String convertTableToSql(String tableName, Datasource ds) {
        return super.convertTableToSql(tableName, ds);
    }

    @Override
    public String transMysqlFilterTerm(String term) {
        return super.transMysqlFilterTerm(term);
    }

    @Override
    public String transCustomFilterList(SQLObj tableObj, List<ChartFieldCustomFilterDTO> requestList) {
        return super.transCustomFilterList(tableObj, requestList);
    }

    @Override
    public String transExtFilterList(SQLObj tableObj, List<ChartExtFilterRequest> requestList) {
        return super.transExtFilterList(tableObj, requestList);
    }

    @Override
    public String sqlFix(String sql) {
        return super.sqlFix(sql);
    }

    @Override
    public String sqlForPreview(String table, Datasource ds) {
        return super.sqlForPreview(table, ds);
    }

    @Override
    public List<Dateformat> dateformat() {
        return super.dateformat();
    }
}
