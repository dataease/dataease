package io.dataease.controller.request.chart;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author gin
 * @Date 2021/4/19 11:29 上午
 */
@Getter
@Setter
public class ChartExtRequest {
    @ApiModelProperty("视图额外过滤条件集合")
    private List<ChartExtFilterRequest> filter;

    //联动过滤条件
    @ApiModelProperty("联动过滤条件集合")
    private List<ChartExtFilterRequest> linkageFilters;

    @ApiModelProperty("下钻维度集合")
    private List<ChartDrillRequest> drill;
}
