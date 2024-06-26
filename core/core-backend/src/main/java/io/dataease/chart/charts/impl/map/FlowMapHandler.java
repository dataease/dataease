package io.dataease.chart.charts.impl.map;

import io.dataease.chart.charts.impl.GroupChartHandler;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class FlowMapHandler extends GroupChartHandler {
    @Getter
    private String type = "flow-map";
}
