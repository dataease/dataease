package io.dataease.api.chart.request;

import io.dataease.api.chart.dto.ChartDimensionDTO;
import lombok.Data;

import java.util.List;

/**
 * @Author gin
 */
@Data
public class ChartDrillRequest {
    private List<ChartDimensionDTO> dimensionList;
}
