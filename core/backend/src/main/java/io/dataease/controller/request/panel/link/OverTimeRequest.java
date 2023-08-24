package io.dataease.controller.request.panel.link;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class OverTimeRequest {

    @ApiModelProperty("资源ID")
    private String resourceId;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("过期时间")
    private Long overTime;
    
}
