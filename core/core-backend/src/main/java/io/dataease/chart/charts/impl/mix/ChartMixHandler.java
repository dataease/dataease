package io.dataease.chart.charts.impl.mix;

import io.dataease.chart.charts.impl.YoyChartHandler;
import io.dataease.extensions.view.dto.AxisFormatResult;
import io.dataease.chart.charts.impl.DefaultChartHandler;
import io.dataease.extensions.view.dto.ChartViewDTO;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class ChartMixHandler extends DefaultChartHandler {
    @Getter
    private final String type = "chart-mix";

    @Override
    public AxisFormatResult formatAxis(ChartViewDTO view) {
        var result = super.formatAxis(view);
        return result;
    }

}
