package io.dataease.base.domain;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PanelSubject implements Serializable {
    @ApiModelProperty("ID")
    private String id;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("类型")
    private String type;
    @ApiModelProperty("创建时间")
    private Long createTime;
    @ApiModelProperty("创建者")
    private String createBy;
    @ApiModelProperty("更新时间")
    private Long updateTime;
    @ApiModelProperty("更新者")
    private String updateBy;
    @ApiModelProperty("详息")
    private String details;

    private static final long serialVersionUID = 1L;
}