package io.dataease.plugins.common.base.domain;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DatasetTableTaskLog implements Serializable {
    @ApiModelProperty("ID")
    private String id;
    @ApiModelProperty("表ID")
    private String tableId;
    @ApiModelProperty("任务ID")
    private String taskId;
    @ApiModelProperty("开始时间")
    private Long startTime;
    @ApiModelProperty("结束时间")
    private Long endTime;
    @ApiModelProperty("状态")
    private String status;
    @ApiModelProperty("创建时间")
    private Long createTime;
    @ApiModelProperty("触发类型")
    private String triggerType;
    @ApiModelProperty("信息")
    private String info;

    private static final long serialVersionUID = 1L;
}