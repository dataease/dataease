package io.dataease.controller.request.panel.link;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class LinkRequest {
    @ApiModelProperty("资源ID")
    private String resourceId;
    @ApiModelProperty("是否有效")
    private boolean valid;
}
