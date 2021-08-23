package io.dataease.base.domain;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PanelGroup implements Serializable {
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
    @ApiModelProperty("类型")
    private String panelType;
    @ApiModelProperty("源")
    private String source;
    @ApiModelProperty("拓展1")
    private String extend1;
    @ApiModelProperty("拓展2")
    private String extend2;
    @ApiModelProperty("重新标记")
    private String remark;

    private static final long serialVersionUID = 1L;
}