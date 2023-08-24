package io.dataease.plugins.common.base.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class DatasetRowPermissionsTree implements Serializable {
    @ApiModelProperty("ID")
    private String id;
    @ApiModelProperty("是否禁用")
    private Boolean enable;
    @ApiModelProperty("授权主体的类型：dept/role/user/sysParams")
    private String authTargetType;
    @ApiModelProperty("授权主体的ID：组织ID/角色ID/用户ID，authTargetType为sysParams时，该值传null")
    private Long authTargetId;
    @ApiModelProperty("数据集ID")
    private String datasetId;
    @ApiModelProperty("修改时间")
    private Long updateTime;

    private static final long serialVersionUID = 1L;
}
