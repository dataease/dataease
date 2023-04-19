package io.dataease.api.permissions.auth.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class BusiPermissionRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -2424587989223319563L;

    @ApiModelProperty("用户/角色ID")
    private Long id;
    @ApiModelProperty("类型(0用户1角色)")
    private Integer type;
    @ApiModelProperty("资源类型")
    private String flag;
}
