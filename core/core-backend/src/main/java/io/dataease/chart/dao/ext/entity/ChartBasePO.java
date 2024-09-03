package io.dataease.chart.dao.ext.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ChartBasePO implements Serializable {
    @Serial
    private static final long serialVersionUID = 183064537525500481L;

    private Long chartId;

    private String chartType;

    private String chartName;

    private Long resourceId;

    private String resourceType;

    private String resourceName;

    private Long tableId;

    private String xAxis;


    private String xAxisExt;


    private String yAxis;


    private String yAxisExt;


    private String extStack;


    private String extBubble;

    private String flowMapStartName;

    private String flowMapEndName;

    private String extColor;

    private String extLabel;

    private String extTooltip;
}
