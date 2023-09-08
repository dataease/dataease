package io.dataease.dto.chart;

import lombok.Data;
import java.util.List;

/**
 * @Author gin
 * @Date 2021/8/9 10:14 上午
 */
@Data
public class ScatterChartDataDTO {
    private Object[] value;
    private List<ChartDimensionDTO> dimensionList;
    private List<ChartQuotaDTO> quotaList;
}
