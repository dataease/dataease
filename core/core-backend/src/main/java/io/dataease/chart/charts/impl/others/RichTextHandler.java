package io.dataease.chart.charts.impl.others;

import io.dataease.chart.charts.impl.YoyChartHandler;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class RichTextHandler extends YoyChartHandler {
    @Getter
    private String type = "rich-text";
    @Getter
    private String render = "custom";
}
