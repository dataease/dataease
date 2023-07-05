package io.dataease.api.ds.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ExcelSheetData {
    private String excelLabel;
    private List<String[]> data;
    private List<TableField> fields;
    private String tableName;
    private String deTableName;
    private String path;
    private boolean isSheet = true;
    private String sheetId;
    private String sheetExcelId;
    private List<Map<String, Object>> jsonArray;

}
