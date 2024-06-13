package io.dataease.api.chart.dto;

import io.dataease.extensions.view.dto.ChartDimensionDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author gin
 */
@Data
public class AxisChartDataDTO {
    private BigDecimal value;
    private List<ChartDimensionDTO> dimensionList;
    private List<ChartQuotaDTO> quotaList;
}
