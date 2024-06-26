package io.dataease.chart.charts.impl.others;

import io.dataease.chart.charts.impl.GroupChartHandler;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class SankeyHandler extends GroupChartHandler {
    @Getter
    private String type = "sankey";
}
