package io.dataease.base.domain;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ChartView implements Serializable {
    @ApiModelProperty("ID")
    private String id;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("分组ID")
    private String sceneId;
    @ApiModelProperty("数据集ID")
    private String tableId;
    @ApiModelProperty("图表类型")
    private String type;
    @ApiModelProperty("chart渲染方式")
    private String render;
    @ApiModelProperty("展示结果")
    private Integer resultCount;
    @ApiModelProperty("展示模式")
    private String resultMode;
    @ApiModelProperty("标题")
    private String title;
    @ApiModelProperty("创建人")
    private String createBy;
    @ApiModelProperty("创建时间")
    private Long createTime;
    @ApiModelProperty("更新时间")
    private Long updateTime;
    @ApiModelProperty("样式优先级")
    private String stylePriority;

    private static final long serialVersionUID = 1L;
}