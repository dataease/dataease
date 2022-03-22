package io.dataease.controller.sys.response;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BasicInfo implements Serializable {

    @ApiModelProperty("请求超时时间")
    private String frontTimeOut;
    @ApiModelProperty("消息保留时间")
    private String msgTimeOut;
    @ApiModelProperty("显示首页")
    private String openHomePage;

}
