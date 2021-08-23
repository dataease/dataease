package io.dataease.controller.request.chart;

import io.dataease.base.domain.DatasetTableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author gin
 * @Date 2021/4/19 10:24 上午
 */
@Getter
@Setter
public class ChartExtFilterRequest {
    @ApiModelProperty("过滤组件ID")
    private String componentId;
    @ApiModelProperty("过滤字段ID")
    private String fieldId;
    @ApiModelProperty("过滤操作符")
    private String operator;
    @ApiModelProperty("过滤值")
    private List<String> value;
    @ApiModelProperty("目标视图ID集合")
    private List<String> viewIds;
    @ApiModelProperty("过滤字段")
    private DatasetTableField datasetTableField;
}
