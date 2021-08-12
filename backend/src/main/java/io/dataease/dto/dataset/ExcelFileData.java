package io.dataease.dto.dataset;

import io.dataease.datasource.dto.TableFiled;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ExcelFileData {
    private String excelId;
    private String excelLable;
    private List<ExcelSheetData> sheets;
    private String path;
    private boolean isSheet = false;
}
