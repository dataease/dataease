package io.dataease.plugins.common.base.domain;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("消息类型")
public class SysMsgType implements Serializable {

    @ApiModelProperty("消息类型ID")
    private Long msgTypeId;

    @ApiModelProperty("上级类型ID")
    private Long pid;

    @ApiModelProperty("消息类型名称")
    private String typeName;

    @ApiModelProperty("回调路由")
    private String router;

    @ApiModelProperty("回调函数")
    private String callback;

    private static final long serialVersionUID = 1L;
}