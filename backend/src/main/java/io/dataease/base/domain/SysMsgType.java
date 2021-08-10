package io.dataease.base.domain;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SysMsgType implements Serializable {

    @ApiModelProperty("消息类型ID")
    private Long msgTypeId;

    @ApiModelProperty("上级类型ID")
    private Long pid;

    @ApiModelProperty("消息类型名称")
    private String typeName;

    @ApiModelProperty(hidden = true)
    private String router;

    @ApiModelProperty(hidden = true)
    private String callback;

    private static final long serialVersionUID = 1L;
}