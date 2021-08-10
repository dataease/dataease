package io.dataease.controller.request.chart;

import io.dataease.dto.chart.ChartDimensionDTO;
import lombok.Data;

import java.util.List;

/**
 * @Author gin
 * @Date 2021/8/10 12:25 下午
 */
@Data
public class ChartDrillRequest {
    private List<ChartDimensionDTO> dimensionList;
}
