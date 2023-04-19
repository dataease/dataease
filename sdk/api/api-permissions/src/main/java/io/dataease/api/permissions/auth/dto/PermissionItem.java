package io.dataease.api.permissions.auth.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PermissionItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 779733339089580705L;
    @ApiModelProperty("资源ID")
    private Long resourceId;
    @ApiModelProperty("权重")
    private int weight;
}
