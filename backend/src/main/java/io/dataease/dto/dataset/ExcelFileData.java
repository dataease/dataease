package io.dataease.dto.dataset;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class ExcelFileData {
    @ApiModelProperty("excelID")
    private String id;
    @ApiModelProperty("excel标签")
    private String excelLabel;
    @ApiModelProperty("sheets")
    private List<ExcelSheetData> sheets;
    @ApiModelProperty("路径")
    private String path;
    @ApiModelProperty("是否Sheet")
    private boolean isSheet = false;
}
