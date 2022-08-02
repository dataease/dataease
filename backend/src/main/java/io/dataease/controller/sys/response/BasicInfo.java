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
    @ApiModelProperty("默认登录方式")
    private Integer loginType = 0;
    @ApiModelProperty("模板市场链接")
    private String templateMarketUlr;
    @ApiModelProperty("模板市场AccessKey")
    private String templateAccessKey;
    @ApiModelProperty("显示模板市场")
    private String openMarketPage;

}
