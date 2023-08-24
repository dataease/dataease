package io.dataease.plugins.common.base.domain;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DatasetTableTask implements Serializable {
    @ApiModelProperty("ID")
    private String id;
    @ApiModelProperty("表ID")
    private String tableId;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("类型")
    private String type;
    @ApiModelProperty("开始时间")
    private Long startTime;
    @ApiModelProperty("频率")
    private String rate;
    @ApiModelProperty("表达式")
    private String cron;
    @ApiModelProperty("结束")
    private String end;
    @ApiModelProperty("结束时间")
    private Long endTime;
    @ApiModelProperty("创建时间")
    private Long createTime;
    @ApiModelProperty("上次执行时间")
    private Long lastExecTime;
    @ApiModelProperty("状态")
    private String status;
    @ApiModelProperty("上次执行状态")
    private String lastExecStatus;
    @ApiModelProperty("抽取数据")
    private String extraData;

    private static final long serialVersionUID = 1L;
}