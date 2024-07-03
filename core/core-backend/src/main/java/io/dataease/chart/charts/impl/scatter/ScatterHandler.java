package io.dataease.chart.charts.impl.scatter;

import io.dataease.chart.charts.impl.YoyChartHandler;
import io.dataease.chart.utils.ChartDataBuild;
import io.dataease.extensions.view.dto.*;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class ScatterHandler extends YoyChartHandler {
    @Getter
    private String type = "scatter";

    @Override
    public AxisFormatResult formatAxis(ChartViewDTO view) {
        var result = super.formatAxis(view);
        var yAxis = new ArrayList<>(view.getYAxis());
        yAxis.addAll(view.getExtBubble());
        yAxis.addAll(view.getExtLabel());
        yAxis.addAll(view.getExtTooltip());
        result.getAxisMap().put(ChartAxis.yAxis, yAxis);
        result.getAxisMap().put(ChartAxis.extBubble, view.getExtBubble());
        result.getAxisMap().put(ChartAxis.extTooltip, view.getExtTooltip());
        result.getAxisMap().put(ChartAxis.extLabel, view.getExtLabel());
        return result;
    }

    @Override
    public Map<String, Object> buildNormalResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, List<String[]> data) {
        boolean isDrill = filterResult.getFilterList().stream().anyMatch(ele -> ele.getFilterType() == 1);
        var xAxis = formatResult.getAxisMap().get(ChartAxis.xAxis);
        var yAxis = formatResult.getAxisMap().get(ChartAxis.yAxis);
        var yAxisTemp = new ArrayList<>(yAxis);
        var extBubble = formatResult.getAxisMap().get(ChartAxis.extBubble);
        if (!extBubble.isEmpty()) {
            // 剔除气泡大小，移除一个
            Iterator<ChartViewFieldDTO> iterator = yAxisTemp.iterator();
            while (iterator.hasNext()) {
                ChartViewFieldDTO obj = iterator.next();
                if (obj.getId().equals(extBubble.getFirst().getId())) {
                    iterator.remove();
                    break;
                }
            }
        }
        Map<String, Object> result = ChartDataBuild.transScatterDataAntV(xAxis, yAxisTemp, view, data, extBubble, isDrill);
        return result;
    }
}
