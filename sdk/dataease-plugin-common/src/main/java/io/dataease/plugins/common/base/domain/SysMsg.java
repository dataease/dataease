package io.dataease.plugins.common.base.domain;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SysMsg implements Serializable {
    @ApiModelProperty("消息ID")
    private Long msgId;
    @ApiModelProperty("用户ID")
    private Long userId;
    @ApiModelProperty("类型ID")
    private Long typeId;
    @ApiModelProperty("状态")
    private Boolean status;
    @ApiModelProperty("参数")
    private String param;
    @ApiModelProperty("创建时间")
    private Long createTime;
    @ApiModelProperty("读取时间")
    private Long readTime;
    @ApiModelProperty("内容")
    private String content;

    private static final long serialVersionUID = 1L;
}