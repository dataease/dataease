package io.dataease.plugins.common.base.domain;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PanelTemplate implements Serializable {
    @ApiModelProperty("ID")
    private String id;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("父ID")
    private String pid;
    @ApiModelProperty("级别")
    private Integer level;
    @ApiModelProperty("节点类型")
    private String nodeType;
    @ApiModelProperty("创建者")
    private String createBy;
    @ApiModelProperty("创建时间")
    private Long createTime;
    @ApiModelProperty("模版类型")
    private String templateType;

    private static final long serialVersionUID = 1L;
}