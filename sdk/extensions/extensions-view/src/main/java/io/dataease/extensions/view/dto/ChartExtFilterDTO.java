package io.dataease.extensions.view.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
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
    @JsonIgnore
    private List<String> originValue;
    private int filterType;// 0-过滤组件，1-下钻，2-联动，外部参数
}
