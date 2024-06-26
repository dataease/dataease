package io.dataease.chart.charts.impl.others;

import io.dataease.chart.charts.impl.DefaultChartHandler;
import io.dataease.chart.charts.impl.ExtQuotaChartHandler;
import io.dataease.extensions.view.dto.AxisFormatResult;
import io.dataease.extensions.view.dto.ChartAxis;
import io.dataease.extensions.view.dto.ChartViewDTO;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class WordCloudHandler extends ExtQuotaChartHandler {
    @Getter
    private String type = "word-cloud";
}
