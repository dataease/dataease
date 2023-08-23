package io.dataease.controller.sys.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class BatchSettingRequest implements Serializable {
    @ApiModelProperty("消息类型ID集合")
    private List<Long> typeIds;
    @ApiModelProperty("消息类型ID")
    private Long channelId;
    @ApiModelProperty("订阅状态")
    private Boolean enable;
}
