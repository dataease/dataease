package io.dataease.api.ds.vo;

import lombok.Data;

import java.util.List;

@Data
public class ExcelConfiguration {
    private String url;
    private List<ExcelSheetData> sheets;
    private String userName;
    private String passwd;
}
