package io.dataease.api.permissions.auth.dto;

import io.dataease.api.permissions.auth.vo.PermissionItem;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class MenuTargetPerCreator extends TargetPerCreator{

    @ApiModelProperty("权限集合")
    private List<PermissionItem> permissions;
}
