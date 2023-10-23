package io.dataease.api.permissions.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Schema(description = "权限结点")
@Data
public class PermissionVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 7951267541124410580L;

    @Schema(description = "是否根结点")
    private boolean root;
    @Schema(description = "是否只读")
    private boolean readonly;
    @Schema(description = "直接权限项")
    private List<PermissionItem> permissions;
    @Schema(description = "关联权限项")
    private List<PermissionOrigin> permissionOrigins;
}
