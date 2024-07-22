package io.dataease.chart.charts.impl.map;

import io.dataease.chart.charts.impl.ExtQuotaChartHandler;
import io.dataease.extensions.view.dto.AxisFormatResult;
import io.dataease.extensions.view.dto.ChartViewDTO;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class BubbleMapHandler extends ExtQuotaChartHandler {
    @Getter
    private String type = "bubble-map";

    @Override
    public AxisFormatResult formatAxis(ChartViewDTO view) {
        return super.formatAxis(view);
    }
}


