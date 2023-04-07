package io.dataease.api.permissions.org.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class OrgCreator implements Serializable {

    @Serial
    private static final long serialVersionUID = -4246980891732805368L;

    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("父ID")
    private Long pid;
}
