package io.dataease.chart.charts.impl.mix;

import io.dataease.chart.utils.ChartDataBuild;
import io.dataease.extensions.view.dto.AxisFormatResult;
import io.dataease.extensions.view.dto.ChartAxis;
import io.dataease.extensions.view.dto.ChartViewDTO;
import io.dataease.extensions.view.dto.CustomFilterResult;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DualLineMixHandler extends GroupMixHandler {
    @Getter
    private final String type = "chart-mix-dual-line";

    @Override
    public Map<String, Object> buildNormalResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, List<String[]> data) {
        boolean isDrill = filterResult
                .getFilterList()
                .stream()
                .anyMatch(ele -> ele.getFilterType() == 1);
        var xAxis = formatResult.getAxisMap().get(ChartAxis.xAxis);
        var xAxisExt = formatResult.getAxisMap().get(ChartAxis.xAxisExt);
        var yAxis = formatResult.getAxisMap().get(ChartAxis.yAxis);
        var xAxisBase = xAxis.subList(0, xAxis.size() - xAxisExt.size());
        return ChartDataBuild.transMixChartDataAntV(xAxisBase, xAxis, xAxisExt, yAxis, view, data, isDrill, true);
    }

}
