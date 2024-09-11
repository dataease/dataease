package io.dataease.chart.charts.impl.mix;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class DualLineMixHandler extends GroupMixHandler {
    @Getter
    private final String type = "chart-mix-dual-line";

}
