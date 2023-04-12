package io.dataease.api.permissions.role.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RoleDetailVO extends RoleCreator{

    @ApiModelProperty("角色ID")
    private Long id;
}
