package io.dataease.controller.sys.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("更新状态参数")
public class SysUserStateRequest implements Serializable {

    @ApiModelProperty(value = "用户ID", required = true)
    private Long userId;

    @ApiModelProperty(value = "状态{1:可用, 0:禁用}", required = true, allowableValues = "1,0")
    private Long enabled;
}
