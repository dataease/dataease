package io.dataease.plugins.view.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PluginChartFieldCustomFilter extends PluginChartViewFieldBase implements Serializable {

    private List<PluginChartCustomFilterItem> filter;

    private PluginDatasetTableField field;

    private List<String> enumCheckField;
}
