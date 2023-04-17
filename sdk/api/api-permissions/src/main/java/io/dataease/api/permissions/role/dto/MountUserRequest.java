package io.dataease.api.permissions.role.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class MountUserRequest implements Serializable {

    @ApiModelProperty("角色ID")
    private Long rid;
    @ApiModelProperty("用户ID集合")
    private List<Long> uids;
}
