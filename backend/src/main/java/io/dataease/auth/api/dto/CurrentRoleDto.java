package io.dataease.auth.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class CurrentRoleDto implements Serializable {

    @ApiModelProperty("ID")
    private Long id;

    @ApiModelProperty("名称")
    private String name;
}
