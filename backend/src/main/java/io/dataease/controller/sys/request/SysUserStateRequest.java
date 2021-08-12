package io.dataease.controller.sys.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class SysUserStateRequest implements Serializable {

    @ApiModelProperty(value = "用户ID", required = true)
    private Long userId;

    @ApiModelProperty(value = "状态", required = true, allowableValues = "1,0")
    private Long enabled;
}
