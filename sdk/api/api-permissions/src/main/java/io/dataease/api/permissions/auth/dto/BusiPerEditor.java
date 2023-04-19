package io.dataease.api.permissions.auth.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class BusiPerEditor extends BusiPermissionRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 3067994331757489447L;

    @ApiModelProperty("权限集合")
    private List<PermissionItem> permissions;
}
