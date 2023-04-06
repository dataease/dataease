package io.dataease.api.permissions.role.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class RoleVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 3488550489306534641L;

    @ApiModelProperty("ID")
    private Long id;
    @ApiModelProperty("名称")
    private String name;
}
