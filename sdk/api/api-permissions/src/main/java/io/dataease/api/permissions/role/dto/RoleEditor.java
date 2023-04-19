package io.dataease.api.permissions.role.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class RoleEditor implements Serializable {
    @Serial
    private static final long serialVersionUID = -4071819873019095722L;

    @ApiModelProperty("ID")
    private Long id;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("描述")
    private String desc;

}
