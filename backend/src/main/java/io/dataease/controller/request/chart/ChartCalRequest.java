package io.dataease.controller.request.chart;

import io.dataease.base.domain.ChartViewWithBLOBs;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author gin
 * @Date 2021/11/10 11:27 上午
 */
@Data
public class ChartCalRequest {
    @ApiModelProperty("视图")
    private ChartViewWithBLOBs view;
    @ApiModelProperty("额外请求参数")
    private ChartExtRequest requestList;
}
