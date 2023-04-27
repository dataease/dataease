package io.dataease.api.permissions.auth.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class PermissionOrigin implements Serializable {
    @Serial
    private static final long serialVersionUID = 1455588932869130794L;

    @JsonSerialize(using= ToStringSerializer.class)
    @ApiModelProperty("角色ID")
    private Long id;
    @ApiModelProperty("角色名称")
    private String name;
    @ApiModelProperty("角色权限")
    private List<PermissionItem> permissions;
}
