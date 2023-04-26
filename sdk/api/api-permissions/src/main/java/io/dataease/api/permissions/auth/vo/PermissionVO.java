package io.dataease.api.permissions.auth.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class PermissionVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 7951267541124410580L;

    @ApiModelProperty("默认角色")
    private boolean root;
    @ApiModelProperty("只读角色")
    private boolean readonly;
    @ApiModelProperty("权限集合")
    private List<PermissionItem> permissions;
    @ApiModelProperty("权限来源")
    private PermissionOrigin permissionOrigin;
}
