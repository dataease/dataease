package io.dataease.api.chart.dto;

import io.dataease.extensions.view.dto.ChartDimensionDTO;
import lombok.Data;

import java.util.List;

/**
 * @Author gin
 */
@Data
public class ScatterChartDataDTO {
    private Object[] value;
    private List<ChartDimensionDTO> dimensionList;
    private List<ChartQuotaDTO> quotaList;
}
