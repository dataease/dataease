package io.dataease.plugins.common.base.domain;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SysRole implements Serializable {
    @ApiModelProperty("角色ID")
    private Long roleId;
    @ApiModelProperty("角色名称")
    private String name;
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("创建者")
    private String createBy;
    @ApiModelProperty("更新者")
    private String updateBy;
    @ApiModelProperty("创建时间")
    private Long createTime;
    @ApiModelProperty("更新时间")
    private Long updateTime;

    private static final long serialVersionUID = 1L;
}