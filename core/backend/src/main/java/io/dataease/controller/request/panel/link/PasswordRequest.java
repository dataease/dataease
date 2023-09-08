package io.dataease.controller.request.panel.link;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PasswordRequest {

    @ApiModelProperty("资源ID")
    private String resourceId;
    @ApiModelProperty("用户ID")
    private Long user = null;
    @ApiModelProperty("密码")
    private String password;


}
