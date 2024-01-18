package io.dataease.api.permissions.auth.dto;

import io.dataease.api.permissions.auth.vo.PermissionItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
@EqualsAndHashCode(callSuper = true)
@Schema(description = "菜单权限构造器")
@Data
public class MenuTargetPerCreator extends TargetPerCreator{

    @Schema(description = "权限集合")
    private List<PermissionItem> permissions;
}
