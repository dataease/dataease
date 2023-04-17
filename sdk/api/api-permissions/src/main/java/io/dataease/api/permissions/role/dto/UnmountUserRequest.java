package io.dataease.api.permissions.role.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UnmountUserRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1361046648092771178L;
    @ApiModelProperty("角色ID")
    private Long rid;
    @ApiModelProperty("用户ID")
    private Long uid;
}
