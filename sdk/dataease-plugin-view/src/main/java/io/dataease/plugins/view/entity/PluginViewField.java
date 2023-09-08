package io.dataease.plugins.view.entity;

import lombok.Data;

import java.util.List;

@Data
public class PluginViewField extends PluginChartViewFieldBase{

    private String typeField;

    private List<PluginChartCustomFilterItem> filter;


}
