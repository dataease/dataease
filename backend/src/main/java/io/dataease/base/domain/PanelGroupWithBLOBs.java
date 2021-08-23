package io.dataease.base.domain;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PanelGroupWithBLOBs extends PanelGroup implements Serializable {
    @ApiModelProperty("仪表板样式")
    private String panelStyle;
    @ApiModelProperty("仪表板数据")
    private String panelData;

    private static final long serialVersionUID = 1L;
}