package io.dataease.dto.dataset;

import io.dataease.datasource.dto.TableFiled;
import lombok.Data;

import java.util.List;

@Data
public class ExcelSheetData {
    private String sheetName;
    private List<List<String>> data;
    private List<TableFiled> fields;
}
