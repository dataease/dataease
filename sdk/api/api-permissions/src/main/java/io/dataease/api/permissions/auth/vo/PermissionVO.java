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


    private List<PermissionItem> permissions;
    @Schema(description = "关联权限项")
    private List<PermissionOrigin> permissionOrigins;
}
