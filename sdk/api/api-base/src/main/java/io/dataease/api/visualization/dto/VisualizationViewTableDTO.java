package io.dataease.api.visualization.dto;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.view.dto.ChartViewDTO;
import lombok.Data;

import java.util.List;

/**
 * @author : WangJiaHao
 * @date : 2024/3/14 12:42
 */
@Data
public class VisualizationViewTableDTO extends ChartViewDTO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long visualizationId;

    private String baseVisualizationData;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long fieldId;

    private String originName;

    private String fieldName;

    private String fieldType;

    private Integer deType;

    private List<DatasetTableFieldDTO> tableFields;
}
