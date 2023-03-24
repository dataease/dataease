package io.dataease.api.chart.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 视图字段base类，与数据集字段表基本一致
 */
@Data
public class ChartViewFieldBaseDTO implements Serializable {
    private String id;

    private String tableId;

    private String originName;

    private String dataeaseName;

    private String name;

    private String type;

    private Boolean checked;

    private Integer columnIndex;

    private Long lastSyncTime;

    private Integer deType;

    private String summary;

    private String sort;

    private Integer deExtractType;

    private String dateStyle;

    private String datePattern;

    private Integer extField;

    private String chartType;

    private ChartFieldCompareDTO compareCalc;

    private String logic;

    private String filterType;

    private String chartId;

    private String dateFormat;
}
