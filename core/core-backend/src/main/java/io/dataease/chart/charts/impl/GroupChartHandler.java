package io.dataease.chart.charts.impl;


import io.dataease.extensions.view.dto.AxisFormatResult;
import io.dataease.extensions.view.dto.ChartAxis;
import io.dataease.extensions.view.dto.ChartViewDTO;
import io.dataease.extensions.view.dto.ChartViewFieldDTO;

import java.util.ArrayList;

public class GroupChartHandler extends YoyChartHandler {
    @Override
    public AxisFormatResult formatAxis(ChartViewDTO view) {
        var result = super.formatAxis(view);
        var xAxis = new ArrayList<ChartViewFieldDTO>(view.getXAxis());
        xAxis.addAll(view.getXAxisExt());
        result.getAxisMap().put(ChartAxis.xAxis, xAxis);
        return result;
    }
}
