package io.dataease.dto.chart;

import io.dataease.plugins.common.base.domain.DatasetTableField;
import lombok.Data;

/**
 * @Author Junjun
 */
@Data
public class ChartSeniorAssistDTO {
    private String name;
    private String field;
    private String fieldId;
    private String summary;
    private String axis;
    private String value;
    private String lineType;
    private String color;
    private DatasetTableField curField;
    private String fontSize;
}
