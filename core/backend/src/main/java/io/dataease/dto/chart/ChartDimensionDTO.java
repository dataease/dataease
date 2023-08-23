package io.dataease.dto.chart;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author gin
 * @Date 2021/8/9 10:15 上午
 */
@Data
public class ChartDimensionDTO {
    @ApiModelProperty("维度ID")
    private String id;
    @ApiModelProperty("维度值")
    private String value;
}
