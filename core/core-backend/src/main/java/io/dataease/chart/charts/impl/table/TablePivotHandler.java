package io.dataease.chart.charts.impl.table;

import io.dataease.chart.charts.impl.GroupChartHandler;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class TablePivotHandler extends GroupChartHandler {
    @Getter
    private String type = "table-pivot";
}
