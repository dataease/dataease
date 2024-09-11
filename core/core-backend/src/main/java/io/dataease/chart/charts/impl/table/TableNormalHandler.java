package io.dataease.chart.charts.impl.table;

import io.dataease.chart.charts.impl.YoyChartHandler;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * @author jianneng
 * @date 2024/9/11 11:37
 **/
@Component
public class TableNormalHandler extends YoyChartHandler {
    @Getter
    private String type = "table-normal";
}
