package io.dataease.api.permissions.user.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserItem implements Serializable {
    @Serial
    private static final long serialVersionUID = -3423336650739339624L;

    @ApiModelProperty("ID")
    private Long id;
    @ApiModelProperty("名称")
    private String name;
}
