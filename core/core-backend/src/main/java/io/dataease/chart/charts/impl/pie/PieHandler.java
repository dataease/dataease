package io.dataease.chart.charts.impl.pie;

import io.dataease.chart.charts.impl.YoyChartHandler;
import io.dataease.extensions.view.dto.AxisFormatResult;
import io.dataease.extensions.view.dto.ChartAxis;
import io.dataease.extensions.view.dto.ChartViewDTO;
import org.springframework.stereotype.Component;

@Component
public class PieHandler extends YoyChartHandler {
    @Override
    public void init() {
        chartHandlerManager.registerChartHandler(this.getRender(), "pie", this);
        chartHandlerManager.registerChartHandler(this.getRender(), "pie-rose", this);
        chartHandlerManager.registerChartHandler(this.getRender(), "pie-donut", this);
        chartHandlerManager.registerChartHandler(this.getRender(), "pie-donut-rose", this);
    }

    @Override
    public AxisFormatResult formatAxis(ChartViewDTO view) {
        var result = super.formatAxis(view);
        var yAxis = result.getAxisMap().get(ChartAxis.yAxis);
        yAxis.addAll(view.getExtLabel());
        yAxis.addAll(view.getExtTooltip());
        result.getAxisMap().put(ChartAxis.extLabel, view.getExtLabel());
        result.getAxisMap().put(ChartAxis.extTooltip, view.getExtTooltip());
        return result;
    }
}
