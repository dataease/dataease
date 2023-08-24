package io.dataease.controller.sys.request;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class MsgSettingRequest implements Serializable {

    @ApiModelProperty("消息类型ID")
    private Long typeId;

    @ApiModelProperty("消息渠道ID")
    private Long channelId;
}
