package io.dataease.plugins.view.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class PluginChartCustomFilterItem implements Serializable {
    private String fieldId;
    private String term;
    private String value;

}
