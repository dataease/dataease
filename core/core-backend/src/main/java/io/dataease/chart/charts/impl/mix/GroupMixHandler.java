package io.dataease.chart.charts.impl.mix;

import io.dataease.extensions.view.dto.*;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.*;

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
            if (!ChartAxis.xAxisExt.equals(k)) {
                v.forEach(x -> x.setSort("none"));
            }
        });
        axisMap.put(ChartAxis.extLabel, view.getExtLabel());
        axisMap.put(ChartAxis.extTooltip, view.getExtTooltip());
        //图表整体主维度
        var xAxis = new ArrayList<>(view.getXAxis());
        var xAxisGroup = new ArrayList<>(view.getXAxis());
        xAxisGroup.addAll(view.getXAxisExt());
        axisMap.put(ChartAxis.xAxis, xAxisGroup);
        context.put("xAxisBase", xAxis);
        axisMap.put(ChartAxis.drill, new ArrayList<>(view.getDrillFields()));
        return result;
    }

    @Override
    public Map<String, Object> buildNormalResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, List<String[]> data) {
        return super.buildNormalResult(view, formatResult, filterResult, data);
    }

}
