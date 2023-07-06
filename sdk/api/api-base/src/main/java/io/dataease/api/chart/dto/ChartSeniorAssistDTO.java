package io.dataease.api.chart.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * @Author Junjun
 */
@Data
public class ChartSeniorAssistDTO {
    private String name;
    private String field;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fieldId;
    private String summary;
    private String axis;
    private String value;
    private String lineType;
    private String color;
    private ChartViewFieldDTO curField;
    private String fontSize;
}
