package io.dataease.chart.charts.impl.others;

import io.dataease.chart.charts.impl.ExtQuotaChartHandler;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class FunnelHandler extends ExtQuotaChartHandler {
    @Getter
    private String type = "funnel";
}
