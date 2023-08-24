package io.dataease.plugins.view.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PluginAxisChartData {

    private BigDecimal value;
    private List<PluginChartDimension> dimensionList;
    private List<PluginChartQuota> quotaList;
}
