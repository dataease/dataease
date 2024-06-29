package io.dataease.chart.charts.impl.numeric;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class IndicatorHandler extends NumericalChartHandler {
    @Getter
    private String render = "custom";
    @Getter
    private String type = "indicator";
}
