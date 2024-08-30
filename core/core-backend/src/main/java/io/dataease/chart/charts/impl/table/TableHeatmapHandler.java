package io.dataease.chart.charts.impl.table;

import io.dataease.chart.charts.impl.DefaultChartHandler;
import io.dataease.chart.utils.ChartDataBuild;
import io.dataease.engine.sql.SQLProvider;
import io.dataease.engine.trans.Dimension2SQLObj;
import io.dataease.engine.trans.Quota2SQLObj;
import io.dataease.engine.utils.Utils;
import io.dataease.extensions.datasource.dto.DatasourceRequest;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.model.SQLMeta;
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.view.dto.*;
import io.dataease.extensions.view.util.ChartDataUtil;
import io.dataease.extensions.view.util.FieldUtil;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class TableHeatmapHandler extends DefaultChartHandler {

    @Getter
    private String type = "t-heatmap";

    @Override
    public AxisFormatResult formatAxis(ChartViewDTO view) {
        var result =  super.formatAxis(view);
        var xAxis = new ArrayList<ChartViewFieldDTO>(view.getXAxis());
        xAxis.addAll(view.getXAxisExt());
        xAxis.addAll(view.getExtColor());
        result.getAxisMap().put(ChartAxis.xAxis, xAxis);
        return result;
    }

    public Map<String, Object> buildResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, List<String[]> data) {
        var xAxis = formatResult.getAxisMap().get(ChartAxis.xAxis);
        Map<String, Object> result = ChartDataBuild.transChartData( xAxis, new ArrayList<>(), view, data, false);
        return result;
    }

    @Override
    public <T extends ChartCalcDataResult> T calcChartResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, Map<String, Object> sqlMap, SQLMeta sqlMeta, Provider provider) {
        var dsMap = (Map<Long, DatasourceSchemaDTO>) sqlMap.get("dsMap");
        List<String> dsList = new ArrayList<>();
        for (Map.Entry<Long, DatasourceSchemaDTO> next : dsMap.entrySet()) {
            dsList.add(next.getValue().getType());
        }
        boolean needOrder = Utils.isNeedOrder(dsList);
        boolean crossDs = Utils.isCrossDs(dsMap);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDsList(dsMap);
        var xAxis = formatResult.getAxisMap().get(ChartAxis.xAxis);
        var yAxis = formatResult.getAxisMap().get(ChartAxis.yAxis);
        var extColorAxis = view.getExtColor();
        List<ChartViewFieldDTO> yFields = new ArrayList<>();
        if(!extColorAxis.isEmpty() && extColorAxis.getFirst().getId()==-1){
            yFields.addAll(chartViewManege.transFieldDTO(Collections.singletonList(chartViewManege.createCountField(view.getTableId()))));
            yAxis.addAll(yFields);
            xAxis = xAxis.stream().filter(i-> !StringUtils.equalsIgnoreCase(i.getDataeaseName(),yAxis.get(0).getDataeaseName())).toList();
        }
        var allFields = (List<ChartViewFieldDTO>) filterResult.getContext().get("allFields");
        Dimension2SQLObj.dimension2sqlObj(sqlMeta, xAxis, FieldUtil.transFields(allFields), crossDs, dsMap, Utils.getParams(FieldUtil.transFields(allFields)),  view.getCalParams(), pluginManage);
        Quota2SQLObj.quota2sqlObj(sqlMeta, yAxis, FieldUtil.transFields(allFields), crossDs, dsMap, Utils.getParams(FieldUtil.transFields(allFields)),  view.getCalParams(), pluginManage);
        yAxis.clear();
        String querySql = SQLProvider.createQuerySQL(sqlMeta, true, needOrder, view);
        querySql = provider.rebuildSQL(querySql, sqlMeta, crossDs, dsMap);
        datasourceRequest.setQuery(querySql);
        logger.debug("calcite chart sql: " + querySql);
        List<String[]> data = (List<String[]>) provider.fetchResultField(datasourceRequest).get("data");
        //自定义排序
        data = ChartDataUtil.resultCustomSort(xAxis, data);
        //数据重组逻辑可重载
        var result = this.buildResult(view, formatResult, filterResult, data);
        T calcResult = (T) new ChartCalcDataResult();
        calcResult.setData(result);
        calcResult.setContext(filterResult.getContext());
        calcResult.setQuerySql(querySql);
        calcResult.setOriginData(data);
        return calcResult;
    }

}


