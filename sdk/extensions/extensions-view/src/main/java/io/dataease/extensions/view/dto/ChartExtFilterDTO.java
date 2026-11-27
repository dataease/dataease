package io.dataease.extensions.view.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.view.filter.FilterTreeObj;
import lombok.Data;


import java.util.List;

@Data
public class ChartExtFilterDTO {
    private Long componentId;
    private String fieldId;
    private String operator;
    private List<String> value;
    // 日期值来源时区，未传时沿用服务端默认时区
    private String timeZone;
    private List<Long> viewIds;
    private List<SqlVariableDetails> parameters;
    private DatasetTableFieldDTO datasetTableField;
    private Boolean isTree = false;
    // Each path uses the same ordered fields as fieldId.
    private List<List<String>> treePaths;
    // Paths of different depths from the same selection are alternatives.
    private String treeFilterGroup;
    private List<DatasetTableFieldDTO> datasetTableFieldList;
    private String dateStyle;
    private String datePattern;
    @JsonIgnore
    private List<String> originValue;
    private int filterType;// 0-过滤组件，1-下钻，2-联动，外部参数 3-联动 自定义参数
    private FilterTreeObj customFilter;
}
