package io.dataease.api.chart.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author gin
 */
@Data
public class ChartDimensionDTO {
    @ApiModelProperty("维度ID")
    private Long id;
    @ApiModelProperty("维度值")
    private String value;
}
