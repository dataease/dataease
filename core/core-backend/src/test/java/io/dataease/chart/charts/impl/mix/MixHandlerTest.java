package io.dataease.chart.charts.impl.mix;

import io.dataease.extensions.view.dto.*;
import io.dataease.api.chart.dto.Series;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class MixHandlerTest {
    @Test
    public void rightLegendRemainsSubcategoryAfterDrilling() {
        verify(List.of("month"), List.of("region"), List.of("day"),
                new String[]{"2026-09", "华东", "2026-09-01", "12"}, "2026-09-01", "华东");
    }

    @Test
    public void rightLegendRemainsSubcategoryAcrossMultipleDrillLevels() {
        verify(List.of("area"), List.of("status"), List.of("province", "city"),
                new String[]{"华东", "新品", "浙江", "杭州", "12"}, "杭州", "新品");
    }

    @Test
    public void rightLegendWorksBeforeDrillingOrAfterReturningToRoot() {
        verify(List.of("area"), List.of("status"), List.of(),
                new String[]{"华东", "新品", "12"}, "华东", "新品");
    }

    @Test
    public void multipleBaseAndSubcategoryFieldsKeepTheirPositions() {
        verify(List.of("area", "month"), List.of("status", "region"), List.of("day"),
                new String[]{"华东", "2026-09", "新品", "上海", "2026-09-01", "12"},
                "2026-09-01", "新品\n上海");
    }

    @Test
    public void missingSubcategoryUsesMetricName() {
        verify(List.of("area"), List.of(), List.of("province"),
                new String[]{"华东", "浙江", "12"}, "浙江", "销售额");
    }

    private void verify(List<String> baseNames, List<String> categoryNames, List<String> drillNames,
                        String[] row, String expectedName, String expectedCategory) {
        List<ChartViewFieldDTO> base = fields(baseNames);
        List<ChartViewFieldDTO> categories = fields(categoryNames);
        List<ChartViewFieldDTO> axes = new ArrayList<>(base);
        axes.addAll(categories);
        axes.addAll(fields(drillNames));
        Map<ChartAxis, List<ChartViewFieldDTO>> axisMap = new HashMap<>();
        axisMap.put(ChartAxis.xAxis, axes);
        axisMap.put(ChartAxis.xAxisExt, categories);
        axisMap.put(ChartAxis.yAxis, fields(List.of("销售额")));
        Map<String, Object> context = new HashMap<>();
        context.put("xAxisBase", base);
        context.put("isRight", "isRight");
        AxisFormatResult format = new AxisFormatResult(axisMap, context);
        ChartViewDTO view = new ChartViewDTO();
        view.setExtLabel(Collections.emptyList());
        view.setExtTooltip(Collections.emptyList());
        List<ChartExtFilterDTO> filters = new ArrayList<>();
        if (!drillNames.isEmpty()) {
            ChartExtFilterDTO drill = new ChartExtFilterDTO();
            drill.setFilterType(1);
            filters.add(drill);
        }
        CustomFilterResult filter = new CustomFilterResult(filters, new HashMap<>());
        for (MixHandler handler : List.of(new MixHandler(), new GroupMixHandler(), new StackMixHandler())) {
            Map<String, Object> result = handler.buildNormalResult(view, format, filter, Collections.singletonList(row));
            List<Series> series = (List<Series>) result.get("data");
            AxisChartDataAntVDTO point = (AxisChartDataAntVDTO) series.get(0).getData().get(0);
            assertEquals(expectedName, point.getName());
            assertEquals(expectedCategory, point.getCategory());
            assertEquals(new BigDecimal("12"), point.getValue());
            assertEquals(axes.size(), point.getDimensionList().size());
        }
    }

    private List<ChartViewFieldDTO> fields(List<String> names) {
        List<ChartViewFieldDTO> fields = new ArrayList<>();
        for (String name : names) {
            ChartViewFieldDTO field = new ChartViewFieldDTO();
            field.setId((long) name.hashCode());
            field.setName(name);
            fields.add(field);
        }
        return fields;
    }
}
