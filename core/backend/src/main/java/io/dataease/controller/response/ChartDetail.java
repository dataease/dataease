package io.dataease.controller.response;

import io.dataease.plugins.common.base.domain.ChartViewWithBLOBs;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ChartDetail extends DataSetDetail{
    @ApiModelProperty("视图")
    private ChartViewWithBLOBs chart;
}
