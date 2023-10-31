package io.dataease.dto.chart;

import lombok.Data;

/**
 * @Author LBOO
 */
@Data
public class ChartSeniorThresholdDTO {
    private String field;
    private String term;
    private ChartSeniorAssistDTO targetField;
    private ChartSeniorAssistDTO maxField;
    private ChartSeniorAssistDTO minField;

}
