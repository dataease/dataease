package io.dataease.provider.query.doris;

import io.dataease.plugins.common.base.domain.ChartViewWithBLOBs;
import io.dataease.plugins.common.base.domain.Datasource;
import io.dataease.plugins.common.dto.chart.ChartFieldCustomFilterDTO;
import io.dataease.plugins.common.dto.chart.ChartViewFieldDTO;
import io.dataease.plugins.common.request.chart.ChartExtFilterRequest;
import io.dataease.plugins.common.request.permission.DataSetRowPermissionsTreeDTO;
import io.dataease.plugins.datasource.entity.PageInfo;
import io.dataease.provider.query.mysql.MysqlQueryProvider;
import org.springframework.stereotype.Service;

import java.util.List;


@Service("dorisQueryProvider")
public class DorisQueryProvider extends MysqlQueryProvider {

    public String getSQLWithPage(boolean isTable, String sql, List<ChartViewFieldDTO> xAxis, List<ChartFieldCustomFilterDTO> fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds, ChartViewWithBLOBs view, PageInfo pageInfo) {
        if(isTable){
            return getSQLTableInfo(sql, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, ds, view);
        }else {
            return getSQLAsTmpTableInfo(sql, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, ds, view);
        }
    }

    public String getResultCount(boolean isTable, String sql, List<ChartViewFieldDTO> xAxis, List<ChartFieldCustomFilterDTO> fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds, ChartViewWithBLOBs view) {
        return null;
    }

}
