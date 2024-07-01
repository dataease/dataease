package io.dataease.chart.charts.impl.mix;

import io.dataease.chart.utils.ChartDataBuild;
import io.dataease.extensions.view.dto.*;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class GroupMixHandler extends MixHandler {
    @Getter
    private final String type = "chart-mix-group";

    @Override
    public AxisFormatResult formatAxis(ChartViewDTO view) {
        var axisMap = new HashMap<ChartAxis, List<ChartViewFieldDTO>>();
        var context = new HashMap<String, Object>();
        AxisFormatResult result = new AxisFormatResult(axisMap, context);
        //左轴分组子维度,非分组不需要
        axisMap.put(ChartAxis.xAxisExt, view.getXAxisExt());
        //左轴堆叠子维度,非堆叠不需要
        axisMap.put(ChartAxis.extStack, Collections.emptyList());
        //左轴指标
        axisMap.put(ChartAxis.yAxis, view.getYAxis());
        //右轴分组子维度
        axisMap.put(ChartAxis.extBubble, view.getExtBubble());
        //右轴指标
        axisMap.put(ChartAxis.yAxisExt, view.getYAxisExt());
        //去除除了x轴以外的排序
        axisMap.forEach((k, v) -> {
            v.forEach(x -> x.setSort("none"));
        });
        axisMap.put(ChartAxis.extLabel, view.getExtLabel());
        axisMap.put(ChartAxis.extTooltip, view.getExtTooltip());
        //图表整体主维度
        var xAxis = new ArrayList<>(view.getXAxis());
        var xAxisGroup = new ArrayList<>(view.getXAxis());
        xAxisGroup.addAll(view.getXAxisExt());
        axisMap.put(ChartAxis.xAxis, xAxisGroup);
        context.put("xAxisBase", xAxis);
        return result;
    }

    @Override
    public Map<String, Object> buildNormalResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, List<String[]> data) {
        return super.buildNormalResult(view, formatResult, filterResult, data);
    }

    @Override
    public ChartViewDTO buildChart(ChartViewDTO view, ChartCalcDataResult calcResult, AxisFormatResult formatResult, CustomFilterResult filterResult) {
        var desensitizationList = (Map<String, ColumnPermissionItem>) filterResult.getContext().get("desensitizationList");
        var leftCalcResult = (ChartCalcDataResult) calcResult.getData().get("left");
        var leftFields = new ArrayList<ChartViewFieldDTO>();
        leftFields.addAll(view.getXAxis());
        leftFields.addAll(view.getXAxisExt());
        leftFields.addAll(view.getYAxis());
        var leftOriginData = leftCalcResult.getOriginData();
        var leftTable = ChartDataBuild.transTableNormal(leftFields, view, leftOriginData, desensitizationList);
        mergeAssistField(leftCalcResult.getDynamicAssistFields(), leftCalcResult.getAssistData());
        var leftData = new HashMap<String, Object>(leftTable);
        leftData.putAll(leftCalcResult.getData());
        leftData.put("dynamicAssistLines", leftCalcResult.getDynamicAssistFields());

        var rightCalcResult = (ChartCalcDataResult) calcResult.getData().get("right");
        var rightFields = new ArrayList<ChartViewFieldDTO>();
        rightFields.addAll(view.getXAxis());
        rightFields.addAll(view.getExtBubble());
        rightFields.addAll(view.getYAxisExt());
        var rightOriginData = rightCalcResult.getOriginData();
        var rightTable = ChartDataBuild.transTableNormal(rightFields, view, rightOriginData, desensitizationList);
        mergeAssistField(rightCalcResult.getDynamicAssistFields(), rightCalcResult.getAssistData());
        var rightData = new HashMap<String, Object>(rightTable);
        rightData.putAll(rightCalcResult.getData());
        rightData.put("dynamicAssistLines", rightCalcResult.getDynamicAssistFields());

        var allFields = (List<ChartViewFieldDTO>) filterResult.getContext().get("allFields");
        // 构建结果
        Map<String, Object> chartData = new TreeMap<>();
        chartData.put("left", leftData);
        chartData.put("right", rightData);

        var drillFilters = filterResult.getFilterList().stream().filter(f -> f.getFilterType() == 1).collect(Collectors.toList());
        var isDrill = CollectionUtils.isNotEmpty(drillFilters);
        view.setDrillFilters(drillFilters);
        view.setDrill(isDrill);
        view.setSql(leftCalcResult.getQuerySql());
        view.setData(chartData);
        return view;
    }
}
