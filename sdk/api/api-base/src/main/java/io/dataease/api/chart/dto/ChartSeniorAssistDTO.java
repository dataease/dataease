package io.dataease.api.chart.dto;

import io.dataease.api.dataset.dto.DatasetTableFieldDTO;
import lombok.Data;

/**
 * @Author Junjun
 */
@Data
public class ChartSeniorAssistDTO {
    private String name;
    private String field;
    private Long fieldId;
    private String summary;
    private String axis;
    private String value;
    private String lineType;
    private String color;
    private DatasetTableFieldDTO curField;
    private String fontSize;
}
