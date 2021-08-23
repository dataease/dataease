package io.dataease.controller.sys.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class SysUserDept implements Serializable {
    @ApiModelProperty("组织ID")
    private Long deptId;
    @ApiModelProperty("父ID")
    private Long pid;
    @ApiModelProperty("组织名称")
    private String deptName;
}
