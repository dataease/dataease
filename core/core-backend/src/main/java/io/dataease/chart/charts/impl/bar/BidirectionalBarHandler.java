package io.dataease.chart.charts.impl.bar;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class BidirectionalBarHandler extends ProgressBarHandler {
    @Getter
    private String type = "bidirectional-bar";
}
