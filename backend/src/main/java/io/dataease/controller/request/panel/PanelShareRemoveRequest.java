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
}
