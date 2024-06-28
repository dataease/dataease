package io.dataease.chart.charts.impl.map;

import io.dataease.api.chart.dto.ColumnPermissionItem;
import io.dataease.chart.charts.impl.GroupChartHandler;
import io.dataease.chart.utils.ChartDataBuild;
import io.dataease.dataset.utils.SqlUtils;
import io.dataease.datasource.provider.CalciteProvider;
import io.dataease.engine.sql.SQLProvider;
import io.dataease.engine.trans.Dimension2SQLObj;
import io.dataease.engine.trans.Quota2SQLObj;
import io.dataease.engine.utils.Utils;
import io.dataease.extensions.datasource.dto.DatasourceRequest;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.view.dto.*;
import io.dataease.extensions.view.model.SQLMeta;
import io.dataease.extensions.view.util.ChartDataUtil;
import io.dataease.extensions.view.util.FieldUtil;
import io.dataease.utils.BeanUtils;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SymbolicMapHandler extends GroupChartHandler {
    @Getter
    private String type = "symbolic-map";

    @Override
    public AxisFormatResult formatAxis(ChartViewDTO view) {
        var result = super.formatAxis(view);
        var yAxis = result.getAxisMap().get(ChartAxis.yAxis);
        yAxis.addAll(view.getExtBubble());
        return result;
    }


    private Map<String, Object> customBuildResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, List<String[]> data, List<ChartViewFieldDTO> detailFields, List<String[]> detailData) {
        boolean isDrill = filterResult
                .getFilterList()
                .stream()
                .anyMatch(ele -> ele.getFilterType() == 1);
        var xAxis = formatResult.getAxisMap().get(ChartAxis.xAxis);
        var yAxis = formatResult.getAxisMap().get(ChartAxis.yAxis);
        Map<String, Object> result = ChartDataBuild.transSymbolicMapNormalWithDetail(xAxis, yAxis, data, detailFields, detailData);
        return result;
    }

    @Override
    public <T extends ChartCalcDataResult> T calcChartResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, Map<String, Object> sqlMap, SQLMeta sqlMeta, CalciteProvider provider) {
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
        var allFields = getAllChartFields(view);
        filterResult.getContext().put("allFields", allFields);
        Dimension2SQLObj.dimension2sqlObj(sqlMeta, xAxis, FieldUtil.transFields(allFields), crossDs, dsMap);
        Quota2SQLObj.quota2sqlObj(sqlMeta, yAxis, FieldUtil.transFields(allFields), crossDs, dsMap);
        String querySql = SQLProvider.createQuerySQL(sqlMeta, true, needOrder, view);
        querySql = SqlUtils.rebuildSQL(querySql, sqlMeta, crossDs, dsMap);
        datasourceRequest.setQuery(querySql);
        logger.info("calcite chart sql: " + querySql);
        List<String[]> data = (List<String[]>) provider.fetchResultField(datasourceRequest).get("data");
        // 获取所有字段数据作为数据详情返回
        List<String[]> detailData = new ArrayList<>();
        List<Long> xAxisIds = xAxis.stream().map(ChartViewFieldDTO::getId).toList();
        List<ChartViewFieldDTO> detailFields = new ArrayList<>();
        detailFields.addAll(xAxis);
        detailFields.addAll(allFields.stream().filter(field -> !xAxisIds.contains(field.getId())).toList());
        if (ObjectUtils.isNotEmpty(detailFields)) {
            SQLMeta sqlMeta1 = new SQLMeta();
            BeanUtils.copyBean(sqlMeta1, sqlMeta);
            sqlMeta1.setYFields(new ArrayList<>());
            Dimension2SQLObj.dimension2sqlObj(sqlMeta1, detailFields, FieldUtil.transFields(allFields), crossDs, dsMap);
            String originSql = SQLProvider.createQuerySQL(sqlMeta1, false, needOrder, view);
            originSql = SqlUtils.rebuildSQL(originSql, sqlMeta, crossDs, dsMap);
            datasourceRequest.setQuery(originSql);
            logger.info("calcite detail field sql: " + querySql);
            detailData = (List<String[]>) provider.fetchResultField(datasourceRequest).get("data");
        }
        //自定义排序
        data = ChartDataUtil.resultCustomSort(xAxis, data);
        //快速计算
        quickCalc(xAxis, yAxis, data);
        //数据重组逻辑可重载
        var result = customBuildResult(view, formatResult, filterResult, data, detailFields, detailData);
        T calcResult = (T) new ChartCalcDataResult();
        calcResult.setData(result);
        calcResult.setContext(filterResult.getContext());
        calcResult.setQuerySql(querySql);
        calcResult.setOriginData(data);
        return calcResult;
    }

    @Override
    public ChartViewDTO buildChart(ChartViewDTO view, ChartCalcDataResult calcResult, AxisFormatResult formatResult, CustomFilterResult filterResult) {
        var desensitizationList = (Map<String, ColumnPermissionItem>) filterResult.getContext().get("desensitizationList");
        var xAxis = formatResult.getAxisMap().get(ChartAxis.xAxis);
        var yAxis = formatResult.getAxisMap().get(ChartAxis.yAxis);
        // 如果是表格导出查询 则在此处直接就可以返回
        var extStack = formatResult.getAxisMap().get(ChartAxis.extStack);
        if (view.getIsExcelExport()) {
            Map<String, Object> sourceInfo = ChartDataBuild.transTableNormal(xAxis, yAxis, view, calcResult.getOriginData(), extStack, desensitizationList);
            sourceInfo.put("sourceData", calcResult.getOriginData());
            view.setData(sourceInfo);
            return view;
        }
        // 图表组件可再扩展
        Map<String, Object> mapTableNormal = calcResult.getData();
        var drillFilters = filterResult.getFilterList().stream().filter(f -> f.getFilterType() == 1).collect(Collectors.toList());
        var isDrill = CollectionUtils.isNotEmpty(drillFilters);
        return uniteViewResult(calcResult.getQuerySql(), calcResult.getData(), mapTableNormal, view, isDrill, drillFilters, calcResult.getDynamicAssistFields(), calcResult.getAssistData());
    }
}
