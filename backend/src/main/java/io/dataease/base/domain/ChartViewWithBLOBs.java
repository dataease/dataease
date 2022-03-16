package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ChartViewWithBLOBs extends ChartView implements Serializable {
    private String xAxis;

    private String xAxisExt;

    private String yAxis;

    private String yAxisExt;

    private String extStack;

    private String extBubble;

    private String customAttr;

    private String customStyle;

    private String customFilter;

    private String drillFields;

    private String senior;

    private String snapshot;

    private static final long serialVersionUID = 1L;
}