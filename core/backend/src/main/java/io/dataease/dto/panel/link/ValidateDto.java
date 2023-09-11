package io.dataease.dto.panel.link;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ValidateDto {

    @ApiModelProperty("是否过期")
    private boolean expire;

    @ApiModelProperty("是否可用")
    private boolean valid;
    @ApiModelProperty("是否启用密码")
    private boolean enablePwd;
    @ApiModelProperty("是否通过密码验证")
    private boolean passPwd;
    @ApiModelProperty("资源ID")
    private String resourceId;
    @ApiModelProperty("用户ID")
    private String userId;
}
