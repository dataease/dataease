package io.dataease.controller.sys.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class SysUserRole implements Serializable {
    @ApiModelProperty("角色ID")
    private Long roleId;
    @ApiModelProperty("角色名称")
    private String roleName;
}
