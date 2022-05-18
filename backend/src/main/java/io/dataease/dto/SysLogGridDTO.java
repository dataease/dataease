package io.dataease.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class SysLogGridDTO implements Serializable {

    @ApiModelProperty("操作类型")
    private String opType;

    @ApiModelProperty("资源类型")
    private String sourceType;

    @ApiModelProperty("详细信息")
    private String detail;

    @ApiModelProperty("操作人")
    private String user;

    @ApiModelProperty("操作时间")
    private Long time;
}
