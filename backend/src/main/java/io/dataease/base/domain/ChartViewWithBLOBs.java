package io.dataease.base.domain;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ChartViewWithBLOBs extends ChartView implements Serializable {
    @ApiModelProperty("x轴")
    private String xAxis;
    @ApiModelProperty("y轴")
    private String yAxis;
    @ApiModelProperty("堆叠")
    private String extStack;
    @ApiModelProperty("气泡")
    private String extBubble;
    @ApiModelProperty("客户端属性")
    private String customAttr;
    @ApiModelProperty("客户端样式")
    private String customStyle;
    @ApiModelProperty("客户端过滤条件")
    private String customFilter;
    @ApiModelProperty("下钻字段")
    private String drillFields;
    @ApiModelProperty("快照")
    private String snapshot;

    private static final long serialVersionUID = 1L;
}