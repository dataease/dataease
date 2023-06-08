package io.dataease.api.ds.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ExcelSheetData {
    @ApiModelProperty("标签")
    private String excelLabel;
    @ApiModelProperty("数据集合")
    private List<String[]> data;
    @ApiModelProperty("字段集合")
    private List<TableField> fields;
    @ApiModelProperty("表名")
    private String tableName;
    @ApiModelProperty("路径")
    private String path;
    @ApiModelProperty("是否Sheet")
    private boolean isSheet = true;
    private String sheetId;
    private String sheetExcelId;
    private List<Map<String, Object>> jsonArray;

}
