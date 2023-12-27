package io.dataease.api.permissions.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
@Schema(description = "菜单权限查询条件")
@Data
public class MenuPermissionRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -7609671259840867561L;
    @Schema(description = "ID")
    private Long id;

}
