package io.dataease.provider.query.api;

import io.dataease.plugins.common.base.domain.ChartViewWithBLOBs;
import io.dataease.plugins.common.base.domain.DatasetTableField;
import io.dataease.plugins.common.base.domain.Datasource;
import io.dataease.plugins.common.dto.chart.ChartFieldCustomFilterDTO;
import io.dataease.plugins.common.dto.chart.ChartViewFieldDTO;
import io.dataease.plugins.common.dto.datasource.DeSortField;
import io.dataease.plugins.common.dto.sqlObj.SQLObj;
import io.dataease.plugins.common.request.chart.ChartExtFilterRequest;
import io.dataease.plugins.common.request.permission.DataSetRowPermissionsTreeDTO;
import io.dataease.plugins.common.request.permission.DatasetRowPermissionsTreeItem;
import io.dataease.plugins.datasource.query.QueryProvider;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("apiQueryProvider")
public class ApiProvider extends QueryProvider {
    @Override
    public Integer transFieldType(String field) {
        switch (field) {
            case "0":
                return 0;// 文本
            case "1":
                return 1;// 时间
            case "2":
                return 2;// 整型
            case "3":
                return 3;// 浮点
            case "4":
                return 4;// 布尔
            default:
                return 0;
        }
    }

    @Override
    public String createSQLPreview(String s, String s1) {
        return null;
    }

    @Override
    public String createQuerySQL(String s, List<DatasetTableField> list, boolean b, Datasource datasource, List<ChartFieldCustomFilterDTO> list1, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree) {
        return null;
    }

    @Override
    public String createQuerySQLAsTmp(String s, List<DatasetTableField> list, boolean b, List<ChartFieldCustomFilterDTO> list1, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree) {
        return null;
    }

    @Override
    public String createQueryTableWithPage(String s, List<DatasetTableField> list, Integer integer, Integer integer1, Integer integer2, boolean b, Datasource datasource, List<ChartFieldCustomFilterDTO> list1, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree) {
        return null;
    }

    @Override
    public String createQuerySQLWithPage(String s, List<DatasetTableField> list, Integer integer, Integer integer1, Integer integer2, boolean b, List<ChartFieldCustomFilterDTO> list1, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree) {
        return null;
    }

    @Override
    public String createQueryTableWithLimit(String s, List<DatasetTableField> list, Integer integer, boolean b, Datasource datasource, List<ChartFieldCustomFilterDTO> list1, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree) {
        return null;
    }

    @Override
    public String createQuerySqlWithLimit(String s, List<DatasetTableField> list, Integer integer, boolean b, List<ChartFieldCustomFilterDTO> list1, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree) {
        return null;
    }

    @Override
    public String getSQL(String s, List<ChartViewFieldDTO> list, List<ChartViewFieldDTO> list1, List<ChartFieldCustomFilterDTO> list2, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> list3, Datasource datasource, ChartViewWithBLOBs chartViewWithBLOBs) {
        return null;
    }

    @Override
    public String createQuerySQL(String s, List<DatasetTableField> list, boolean b, Datasource datasource, List<ChartFieldCustomFilterDTO> list1, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<DeSortField> list2) {
        return null;
    }

    @Override
    public String createQuerySQLAsTmp(String s, List<DatasetTableField> list, boolean b, List<ChartFieldCustomFilterDTO> list1, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<DeSortField> list2) {
        return null;
    }

    @Override
    public String getSQLAsTmp(String s, List<ChartViewFieldDTO> list, List<ChartViewFieldDTO> list1, List<ChartFieldCustomFilterDTO> list2, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> list3, ChartViewWithBLOBs chartViewWithBLOBs) {
        return null;
    }

    @Override
    public String getSQLTableInfo(String s, List<ChartViewFieldDTO> list, List<ChartFieldCustomFilterDTO> list1, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> list2, Datasource datasource, ChartViewWithBLOBs chartViewWithBLOBs) {
        return null;
    }

    @Override
    public String getSQLAsTmpTableInfo(String s, List<ChartViewFieldDTO> list, List<ChartFieldCustomFilterDTO> list1, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> list2, Datasource datasource, ChartViewWithBLOBs chartViewWithBLOBs) {
        return null;
    }

    @Override
    public String getSQLStack(String s, List<ChartViewFieldDTO> list, List<ChartViewFieldDTO> list1, List<ChartFieldCustomFilterDTO> list2, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> list3, List<ChartViewFieldDTO> list4, Datasource datasource, ChartViewWithBLOBs chartViewWithBLOBs) {
        return null;
    }

    @Override
    public String getSQLAsTmpStack(String s, List<ChartViewFieldDTO> list, List<ChartViewFieldDTO> list1, List<ChartFieldCustomFilterDTO> list2, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> list3, List<ChartViewFieldDTO> list4, ChartViewWithBLOBs chartViewWithBLOBs) {
        return null;
    }

    @Override
    public String getSQLScatter(String s, List<ChartViewFieldDTO> list, List<ChartViewFieldDTO> list1, List<ChartFieldCustomFilterDTO> list2, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> list3, List<ChartViewFieldDTO> list4, Datasource datasource, ChartViewWithBLOBs chartViewWithBLOBs) {
        return null;
    }

    @Override
    public String getSQLAsTmpScatter(String s, List<ChartViewFieldDTO> list, List<ChartViewFieldDTO> list1, List<ChartFieldCustomFilterDTO> list2, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> list3, List<ChartViewFieldDTO> list4, ChartViewWithBLOBs chartViewWithBLOBs) {
        return null;
    }

    @Override
    public String searchTable(String s) {
        return null;
    }

    @Override
    public String getSQLSummary(String s, List<ChartViewFieldDTO> list, List<ChartFieldCustomFilterDTO> list1, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> list2, ChartViewWithBLOBs chartViewWithBLOBs, Datasource datasource) {
        return null;
    }

    @Override
    public String getSQLSummaryAsTmp(String s, List<ChartViewFieldDTO> list, List<ChartFieldCustomFilterDTO> list1, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> list2, ChartViewWithBLOBs chartViewWithBLOBs) {
        return null;
    }

    @Override
    public String wrapSql(String s) {
        return null;
    }

    @Override
    public String createRawQuerySQL(String s, List<DatasetTableField> list, Datasource datasource) {
        return null;
    }

    @Override
    public String createRawQuerySQLAsTmp(String s, List<DatasetTableField> list) {
        return null;
    }

    @Override
    public String transTreeItem(SQLObj sqlObj, DatasetRowPermissionsTreeItem datasetRowPermissionsTreeItem) {
        return null;
    }
}
