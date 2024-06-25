package io.dataease.chart.charts.impl.map;

import io.dataease.chart.charts.impl.GroupChartHandler;
import io.dataease.extensions.view.dto.AxisFormatResult;
import io.dataease.extensions.view.dto.ChartAxis;
import io.dataease.extensions.view.dto.ChartViewDTO;
import lombok.Getter;
import org.springframework.stereotype.Component;

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
}
