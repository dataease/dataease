package io.dataease.plugins.view.entity;

import io.dataease.plugins.common.dto.chart.ChartFieldCompareDTO;
import lombok.Data;

import java.util.List;

@Data
public class PluginViewField extends PluginChartViewFieldBase {

    private String typeField;

    private List<PluginChartCustomFilterItem> filter;

    private ChartFieldCompareDTO compareCalc;

    private String busiType;
}
