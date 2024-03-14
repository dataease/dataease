package io.dataease.api.visualization.dto;

import io.dataease.api.chart.dto.ChartViewDTO;
import io.dataease.dto.dataset.DatasetTableFieldDTO;
import lombok.Data;

import java.util.List;

/**
 * @author : WangJiaHao
 * @date : 2024/3/14 12:42
 */
@Data
public class VisualizationViewTableDTO extends ChartViewDTO {

    private String visualizationId;

    private String baseVisualizationData;

    private List<DatasetTableFieldDTO> tableFields;
}
