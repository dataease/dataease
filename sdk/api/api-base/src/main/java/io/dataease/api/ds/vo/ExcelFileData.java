package io.dataease.api.ds.vo;

import lombok.Data;

import java.util.List;

@Data
public class ExcelFileData {
    private String id;
    private String excelLabel;
    private List<ExcelSheetData> sheets;
    private String path;
    private boolean isSheet = false;
}
