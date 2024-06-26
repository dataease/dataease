package io.dataease.chart.charts;

import io.dataease.datasource.provider.CalciteProvider;
import io.dataease.extensions.view.dto.*;
import io.dataease.extensions.view.model.SQLMeta;

import java.util.List;
import java.util.Map;

public abstract class AbstractChartHandler {
    public abstract <T> T formatAxis(ChartViewDTO view);
    public abstract <T extends CustomFilterResult, K extends AxisFormatResult> T customFilter(ChartViewDTO view, List<ChartExtFilterDTO> filterList, K formatResult);
    public abstract <T extends ChartCalcDataResult> T calcChartResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, Map<String, Object> sqlMap, SQLMeta sqlMeta, CalciteProvider provider);
    public abstract ChartViewDTO buildChart(ChartViewDTO view, ChartCalcDataResult calcResult, AxisFormatResult formatResult, CustomFilterResult filterResult);
}
