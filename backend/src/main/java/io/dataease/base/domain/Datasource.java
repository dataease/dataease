package io.dataease.base.domain;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class Datasource implements Serializable {
    @ApiModelProperty("ID")
    private String id;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("描述")
    private String desc;
    @ApiModelProperty("类型")
    private String type;
    @ApiModelProperty("创建时间")
    private Long createTime;
    @ApiModelProperty("更新时间")
    private Long updateTime;
    @ApiModelProperty("创建者")
    private String createBy;
    @ApiModelProperty("状态")
    private String status;
    @ApiModelProperty("配置详情")
    private String configuration;

    private static final long serialVersionUID = 1L;
}