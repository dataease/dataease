package io.dataease.datasource.dto;

import io.dataease.api.ds.vo.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class ExcelSheetData {
    @ApiModelProperty("标签")
    private String excelLabel;
    @ApiModelProperty("数据集合")
    private List<List<String>> data;
    @ApiModelProperty("字段集合")
    private List<TableField> fields;
    @ApiModelProperty("表名")
    private String tableName;
    @ApiModelProperty("路径")
    private String path;
}
