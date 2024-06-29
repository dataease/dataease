package io.dataease.chart.charts.impl.bar;

import io.dataease.chart.utils.ChartDataBuild;
import io.dataease.extensions.view.dto.*;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class StackBarHandler extends BarHandler {
    @Getter
    private String type = "bar-stack";
    @Override
    public void init() {
        chartHandlerManager.registerChartHandler(this.getRender(), "bar-stack", this);
        chartHandlerManager.registerChartHandler(this.getRender(), "bar-stack-horizontal", this);
        chartHandlerManager.registerChartHandler(this.getRender(), "percentage-bar-stack", this);
        chartHandlerManager.registerChartHandler(this.getRender(), "percentage-bar-stack-horizontal", this);
    }
    @Override
    public AxisFormatResult formatAxis(ChartViewDTO view) {
        var result = super.formatAxis(view);
        var xAxis = result.getAxisMap().get(ChartAxis.xAxis);
        xAxis.addAll(view.getExtStack());
        result.getAxisMap().put(ChartAxis.extStack, view.getExtStack());
        return result;
    }
    @Override
    public <T extends CustomFilterResult> T customFilter(ChartViewDTO view, List<ChartExtFilterDTO> filterList, AxisFormatResult formatResult) {
        var result = super.customFilter(view, filterList, formatResult);
        List<ChartDrillRequest> drillRequestList = view.getChartExtRequest().getDrill();
        var drillFields = formatResult.getAxisMap().get(ChartAxis.drill);
        // 堆叠维度下钻
        if (ObjectUtils.isNotEmpty(drillRequestList) && (drillFields.size() > drillRequestList.size())) {
            List<ChartExtFilterDTO> noDrillFilterList = filterList
                    .stream()
                    .filter(ele -> ele.getFilterType() != 1)
                    .collect(Collectors.toList());
            var noDrillFieldAxis = formatResult.getAxisMap().get(ChartAxis.xAxis)
                    .stream()
                    .filter(ele -> ele.getSource() != FieldSource.DRILL)
                    .collect(Collectors.toList());
            List<ChartExtFilterDTO> drillFilters = new ArrayList<>();
            ArrayList<ChartViewFieldDTO> fieldsToFilter = new ArrayList<>();
            var extStack = formatResult.getAxisMap().get(ChartAxis.extStack);
            if (ObjectUtils.isNotEmpty(extStack) &&
                    Objects.equals(drillFields.get(0).getId(), extStack.get(0).getId())) {
                fieldsToFilter.addAll(view.getXAxis());
            }
            groupStackDrill(noDrillFieldAxis, noDrillFilterList, fieldsToFilter, drillFields, drillRequestList);
            formatResult.getAxisMap().put(ChartAxis.xAxis, noDrillFieldAxis);
            result.setFilterList(noDrillFilterList);
        }
        return (T) result;
    }
    @Override
    public Map<String, Object> buildNormalResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, List<String[]> data) {
        boolean isDrill = filterResult.getFilterList().stream().anyMatch(ele -> ele.getFilterType() == 1);
        var xAxis = formatResult.getAxisMap().get(ChartAxis.xAxis);
        var extStack = formatResult.getAxisMap().get(ChartAxis.extStack);
        var yAxis = formatResult.getAxisMap().get(ChartAxis.yAxis);
        var xAxisBase = xAxis.subList(0, xAxis.size() - extStack.size());
        return ChartDataBuild.transStackChartDataAntV(xAxisBase, yAxis, view, data, extStack, isDrill);
    }
}
