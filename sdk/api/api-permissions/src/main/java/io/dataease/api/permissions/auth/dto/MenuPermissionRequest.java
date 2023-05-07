package io.dataease.api.permissions.auth.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class MenuPermissionRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -7609671259840867561L;

    @ApiModelProperty("角色ID/菜单ID")
    private Long id;

}
