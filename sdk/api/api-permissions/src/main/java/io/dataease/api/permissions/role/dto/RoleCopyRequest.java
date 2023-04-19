package io.dataease.api.permissions.role.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class RoleCopyRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1631759805936434870L;

    @ApiModelProperty("用户ID")
    private Long copyId;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("描述")
    private String desc;
}
