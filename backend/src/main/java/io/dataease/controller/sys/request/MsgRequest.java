package io.dataease.controller.sys.request;

import io.dataease.controller.sys.base.BaseGridRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("消息条件")
public class MsgRequest extends BaseGridRequest implements Serializable {

    private static final long serialVersionUID = 1920091635946508658L;

    @ApiModelProperty("消息类型ID")
    private Long type;

    @ApiModelProperty("是否订阅")
    private Boolean status;

}
