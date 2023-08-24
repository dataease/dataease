package io.dataease.plugins.xpack.role.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class XpackRoleDto implements Serializable {

    @ApiModelProperty("角色ID")
    private Long roleId;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty(hidden = true)
    private String createBy;
    @ApiModelProperty(hidden = true)
    private String updateBy;
    @ApiModelProperty("创建时间")
    private Long createTime;
    @ApiModelProperty(hidden = true)
    private Long updateTime;
    @ApiModelProperty("关联用户")
    private List<String> users;

    private static final long serialVersionUID = 1L;
}
