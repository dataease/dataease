package io.dataease.plugins.view.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class PluginChartViewFieldBase implements Serializable {

    private String id;

    private String dataeaseName;

    private Integer extField;

    private String originName;

    private String sort;

    private Integer deExtractType;

    private Integer deType;

    private String dateStyle;

    private String datePattern;

    private String type;

    private String summary;

    private String logic;

    private String filterType;

    private String name;

}
