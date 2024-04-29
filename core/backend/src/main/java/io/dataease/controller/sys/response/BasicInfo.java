package io.dataease.controller.sys.response;

import java.io.Serializable;

import io.dataease.plugins.xpack.loginlimit.dto.response.LoginLimitInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BasicInfo extends LoginLimitInfo implements Serializable {

    @ApiModelProperty("请求超时时间")
    private String frontTimeOut;
    @ApiModelProperty("消息保留时间")
    private String msgTimeOut;
    @ApiModelProperty("日志保留时间")
    private String logTimeOut;
    @ApiModelProperty("数据同步日志保留时间")
    private String dsSyncLogTimeOut;
    @ApiModelProperty("后台导出文件保留时间")
    private String exportFileTimeOut;
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
    @ApiModelProperty("数据源检测时间间隔")
    private String dsCheckInterval;
    @ApiModelProperty("数据源检测时间间隔类型")
    private String dsCheckIntervalType;
    @ApiModelProperty("自动识别移动端")
    private String autoMobile = "true";

}
