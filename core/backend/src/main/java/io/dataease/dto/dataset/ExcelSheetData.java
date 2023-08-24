package io.dataease.dto.dataset;

import io.dataease.plugins.common.dto.datasource.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ExcelSheetData {
    @ApiModelProperty("标签")
    private String excelLabel;
    @ApiModelProperty("数据集合")
    private List<List<String>> data;
    @ApiModelProperty("字段集合")
    private List<TableField> fields;
    @ApiModelProperty("是否sheet")
    private boolean isSheet = true;
    @ApiModelProperty("json数组")
    private List<Map<String, Object>> jsonArray;
    @ApiModelProperty("数据集名称")
    private String datasetName;
    @ApiModelProperty("excelID")
    private String sheetExcelId;
    @ApiModelProperty("sheetId")
    private String id;
    @ApiModelProperty("路径")
    private String path;
    @ApiModelProperty("字段MD5")
    private String fieldsMd5;
    @ApiModelProperty("字段变更")
    private Boolean changeFiled = false;
    private Boolean effectExtField = false;
}
