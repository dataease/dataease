package io.dataease.api.permissions.role.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class RoleVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 3488550489306534641L;
    @JsonSerialize(using= ToStringSerializer.class)
    @ApiModelProperty("ID")
    private Long id;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("只读")
    private boolean readonly;
    @ApiModelProperty("是否根角色(组织默认角色)")
    private boolean root;
}
