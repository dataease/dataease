package io.dataease.controller.request.panel.link;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class EnablePwdRequest {

    @ApiModelProperty("资源ID")
    private String resourceId;
    @ApiModelProperty("是否启用密码保护")
    private boolean enablePwd;
}
