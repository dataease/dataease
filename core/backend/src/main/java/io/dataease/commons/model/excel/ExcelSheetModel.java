package io.dataease.commons.model.excel;

import java.util.List;

import lombok.Data;

@Data
public class ExcelSheetModel {

    private String sheetName;

    private List<String> heads;

    private List<List<String>> data;

}
