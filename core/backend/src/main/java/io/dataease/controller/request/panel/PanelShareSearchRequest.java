package io.dataease.controller.request.panel;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PanelShareSearchRequest implements Serializable {

    @ApiModelProperty(value = "分享目标类型", allowableValues = "0:user,1:role,2:dept")
    private String type;

    @ApiModelProperty("仪表板ID")
    private String resourceId;

    @ApiModelProperty("当前用户")
    private String currentUserName;

}
