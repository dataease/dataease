package io.dataease.api.permissions.auth.dto;

import io.dataease.api.permissions.auth.vo.PermissionItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Schema(description = "菜单权限编辑器")
@Data
public class MenuPerEditor extends MenuPermissionRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 3410520935167596750L;
    @Schema(description = "菜单权限集合")
    private List<PermissionItem> permissions;
}
