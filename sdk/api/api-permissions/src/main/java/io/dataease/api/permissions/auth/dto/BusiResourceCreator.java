package io.dataease.api.permissions.auth.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class BusiResourceCreator implements Serializable {
    @Serial
    private static final long serialVersionUID = 3656747026193567757L;

    @ApiModelProperty("资源名称")
    private String name;
    @ApiModelProperty("类型")
    private String flag;
    @ApiModelProperty("上级ID")
    private Long pid = 0L;
}
