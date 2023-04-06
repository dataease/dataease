package io.dataease.api.permissions.user.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;

@Data
public class UserEditor extends UserCreator{

    @Serial
    private static final long serialVersionUID = 1580870660998152922L;

    @ApiModelProperty("用户ID")
    private Long id;
}
