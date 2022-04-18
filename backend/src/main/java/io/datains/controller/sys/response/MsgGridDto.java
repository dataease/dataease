package io.datains.controller.sys.response;

import io.datains.base.domain.SysMsg;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class MsgGridDto extends SysMsg {

    @ApiModelProperty("回调路由")
    private String router;
    @ApiModelProperty("回调函数")
    private String callback;
}
