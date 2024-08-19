package io.dataease.chart.charts.impl.numeric;

import io.dataease.chart.charts.impl.YoyChartHandler;
import io.dataease.chart.utils.ChartDataBuild;
import io.dataease.extensions.datasource.model.SQLMeta;
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.view.dto.*;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class IndicatorHandler extends YoyChartHandler {
    @Getter
    private String render = "custom";
    @Getter
    private String type = "indicator";

    @Override
    public Map<String, Object> buildNormalResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, List<String[]> data) {
        boolean isDrill = filterResult.getFilterList().stream().anyMatch(ele -> ele.getFilterType() == 1);
        var xAxis = formatResult.getAxisMap().get(ChartAxis.xAxis);
        var yAxis = formatResult.getAxisMap().get(ChartAxis.yAxis);
        return ChartDataBuild.transNormalChartData(xAxis, yAxis, view, data, isDrill);
    }

    @Override
    public <T extends ChartCalcDataResult> T calcChartResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, Map<String, Object> sqlMap, SQLMeta sqlMeta, Provider provider) {
        this.setIndicatorHandlerXAxis(formatResult, filterResult);
        return (T) super.calcChartResult(view, formatResult, filterResult, sqlMap, sqlMeta, provider);
    }

    private void setIndicatorHandlerXAxis(AxisFormatResult formatResult, CustomFilterResult filterResult) {
        var xAxis = formatResult.getAxisMap().get(ChartAxis.xAxis);
        var yAxis = formatResult.getAxisMap().get(ChartAxis.yAxis);
        var allFields = (List<ChartViewFieldDTO>) filterResult.getContext().get("allFields");
        ChartViewFieldDTO chartViewFieldDTO = yAxis.get(0);
        ChartFieldCompareDTO compareCalc = chartViewFieldDTO.getCompareCalc();
        boolean isYoy = org.apache.commons.lang3.StringUtils.isNotEmpty(compareCalc.getType())
                && !org.apache.commons.lang3.StringUtils.equalsIgnoreCase(compareCalc.getType(), "none");
        if (isYoy) {
            xAxis.clear();
            // 设置维度字段，从同环比中获取用户选择的字段
            xAxis.addAll(allFields.stream().filter(i -> org.springframework.util.StringUtils.endsWithIgnoreCase(i.getId().toString(), yAxis.get(0).getCompareCalc().getField().toString())).toList());
            xAxis.get(0).setSort("desc");
            if (org.springframework.util.StringUtils.endsWithIgnoreCase("month_mom", compareCalc.getType())) {
                xAxis.get(0).setDateStyle("y_M");
            }
            if (org.springframework.util.StringUtils.endsWithIgnoreCase("day_mom", compareCalc.getType())) {
                xAxis.get(0).setDateStyle("y_M_d");
            }
            if (org.springframework.util.StringUtils.endsWithIgnoreCase("year_mom", compareCalc.getType())) {
                xAxis.get(0).setDateStyle("y");
            }
        }
        formatResult.getAxisMap().put(ChartAxis.xAxis, xAxis);
    }
}
