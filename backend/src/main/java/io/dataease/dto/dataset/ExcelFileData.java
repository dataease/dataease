package io.dataease.dto.dataset;

import lombok.Data;

import java.util.List;

@Data
public class ExcelFileData {
    private String excelId;
    private String excelLable;
    private List<ExcelSheetData> sheets;
    private String path;
    private boolean isSheet = false;
}
