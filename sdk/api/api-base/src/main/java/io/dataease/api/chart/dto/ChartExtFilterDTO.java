package io.dataease.api.chart.dto;

import io.dataease.api.dataset.dto.DatasetTableFieldDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class ChartExtFilterDTO {
    @ApiModelProperty("过滤组件ID")
    private String componentId;
    @ApiModelProperty("过滤字段ID")
    private String fieldId;
    @ApiModelProperty("过滤操作符")
    private String operator;
    @ApiModelProperty("过滤值")
    private List<String> value;
    @ApiModelProperty("目标视图ID集合")
    private List<String> viewIds;
    @ApiModelProperty("sql 数据集参数")
    private List<String> parameters;
    @ApiModelProperty("过滤字段")
    private DatasetTableFieldDTO datasetTableField;
    @ApiModelProperty("是否树")
    private Boolean isTree = false;
    @ApiModelProperty("过滤字段集合")
    private List<DatasetTableFieldDTO> datasetTableFieldList;
    @ApiModelProperty("日期分隔符类型")
    private String dateStyle;
    @ApiModelProperty("日期格式")
    private String datePattern;
}
