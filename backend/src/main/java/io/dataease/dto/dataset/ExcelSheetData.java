package io.dataease.dto.dataset;

import io.dataease.datasource.dto.TableFiled;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ExcelSheetData {
    private String excelLable;
    private List<List<String>> data;
    private List<TableFiled> fields;
    private boolean isSheet = true;
    private List<Map<String, Object>> jsonArray;
    private String datasetName;
    private String sheetExcelId;
    private String sheetId;
    private String path;
    private String fieldsMd5;
}
