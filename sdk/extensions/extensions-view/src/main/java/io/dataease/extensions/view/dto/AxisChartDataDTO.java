package io.dataease.extensions.view.dto;

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
