package io.dataease.api.chart.request;

import io.dataease.api.chart.dto.ChartDimensionDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author gin
 */
@Data
public class ChartDrillRequest {
    @ApiModelProperty("下钻维度集合")
    private List<ChartDimensionDTO> dimensionList;
}
