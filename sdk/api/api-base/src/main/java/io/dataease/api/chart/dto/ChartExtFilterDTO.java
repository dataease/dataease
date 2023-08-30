package io.dataease.api.chart.dto;

import io.dataease.api.dataset.dto.SqlVariableDetails;
import io.dataease.dto.dataset.DatasetTableFieldDTO;
import lombok.Data;

import java.util.List;

@Data
public class ChartExtFilterDTO {
    private Long componentId;
    private String fieldId;
    private String operator;
    private List<String> value;
    private List<Long> viewIds;
    private List<SqlVariableDetails> parameters;
    private DatasetTableFieldDTO datasetTableField;
    private Boolean isTree = false;
    private List<DatasetTableFieldDTO> datasetTableFieldList;
    private String dateStyle;
    private String datePattern;
}
