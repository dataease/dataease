package io.dataease.api.permissions.auth.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BusiTargetPerCreator extends MenuTargetPerCreator{

    @ApiModelProperty("类型(0用户1角色)")
    private Integer type;
    @ApiModelProperty("资源类型")
    private String flag;
}
