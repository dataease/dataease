package io.dataease.chart.charts.impl.numberic;

import io.dataease.chart.charts.impl.DefaultChartHandler;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class IndicatorHandler extends NumbericChartHandler {
    @Getter
    private String render = "custom";
    @Getter
    private String type = "indicator";
}
