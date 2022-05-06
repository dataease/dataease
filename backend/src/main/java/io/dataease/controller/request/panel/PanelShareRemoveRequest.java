package io.dataease.controller.request.panel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("取消分享参数")
public class PanelShareRemoveRequest implements Serializable {

    @ApiModelProperty("仪表板ID")
    private String panelId;

    @ApiModelProperty("分享ID")
    private String shareId;

    @ApiModelProperty("分享类型{0:用户,1:角色,2:组织}")
    private Integer type;

    @ApiModelProperty("目标ID")
    private Long targetId;
}
