package io.dataease.base.domain;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("消息渠道")
public class SysMsgChannel implements Serializable {

    @ApiModelProperty("消息渠道ID")
    private Long msgChannelId;

    @ApiModelProperty("消息渠道名称")
    private String channelName;

    private static final long serialVersionUID = 1L;
}