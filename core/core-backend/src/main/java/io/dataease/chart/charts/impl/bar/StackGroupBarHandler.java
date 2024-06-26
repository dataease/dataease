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

import static io.dataease.extensions.view.dto.ChartAxis.extStack;
import static io.dataease.extensions.view.dto.ChartAxis.xAxisExt;

@Component
public class StackGroupBarHandler extends BarHandler {
    @Getter
    private String type = "bar-group-stack";

    @Override
    public void init() {
        chartHandlerManager.registerChartHandler(this.getRender(), this.getType(), this);
    }

    @Override
    public AxisFormatResult formatAxis(ChartViewDTO view) {
        var result = super.formatAxis(view);
        var xAxis = result.getAxisMap().get(ChartAxis.xAxis);
        xAxis.addAll(view.getXAxisExt());
        xAxis.addAll(view.getExtStack());
        result.getAxisMap().put(ChartAxis.xAxisExt, view.getExtStack());
        result.getAxisMap().put(ChartAxis.extStack, view.getExtStack());
        result.getAxisMap().put(ChartAxis.xAxisExt, view.getXAxisExt());
        return result;
    }

    @Override
    public <T extends CustomFilterResult, K extends AxisFormatResult> T customFilter(ChartViewDTO view, List<ChartExtFilterDTO> filterList, K formatResult) {
        var result = super.customFilter(view, filterList, formatResult);
        List<ChartDrillRequest> drillRequestList = view.getChartExtRequest().getDrill();
        var drillFields = formatResult.getAxisMap().get(ChartAxis.drill);
        // 分组维度下钻
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
            var xAxisExt = formatResult.getAxisMap().get(ChartAxis.xAxisExt);
            var extStack = formatResult.getAxisMap().get(ChartAxis.extStack);
            if (ObjectUtils.isNotEmpty(xAxisExt) && ObjectUtils.isNotEmpty(extStack)) {
                if (Objects.equals(drillFields.get(0).getId(), xAxisExt.get(0).getId())) {
                    fieldsToFilter.addAll(view.getXAxis());
                    fieldsToFilter.addAll(extStack);
                }
                if (Objects.equals(drillFields.get(0).getId(), extStack.get(0).getId())) {
                    fieldsToFilter.addAll(view.getXAxis());
                    fieldsToFilter.addAll(xAxisExt);
                }
            }
            groupStackDrill(noDrillFieldAxis, noDrillFilterList, fieldsToFilter, drillFields, drillRequestList);
            formatResult.getAxisMap().put(ChartAxis.xAxis, noDrillFieldAxis);
            result.setFilterList(noDrillFilterList);
        }
        return (T) result;
    }

    @Override
    public Map<String, Object> buildNormalResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, List<String[]> data) {
        boolean isDrill = filterResult
                .getFilterList()
                .stream()
                .anyMatch(ele -> ele.getFilterType() == 1);
        var xAxis = formatResult.getAxisMap().get(ChartAxis.xAxis);
        var xAxisExt = formatResult.getAxisMap().get(ChartAxis.xAxisExt);
        var extStack = formatResult.getAxisMap().get(ChartAxis.extStack);
        var yAxis = formatResult.getAxisMap().get(ChartAxis.yAxis);
        var xAxisBase = xAxis.subList(0, xAxis.size() - xAxisExt.size() - extStack.size());
        var xAxisMain = xAxis.subList(0, xAxis.size() - extStack.size());
        return ChartDataBuild.transGroupStackDataAntV(xAxisBase, xAxisMain, xAxisExt, yAxis, extStack, data, view, isDrill);
    }
}
