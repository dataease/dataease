package io.dataease.api.ds.vo;

import lombok.Data;

@Data
public class RemoteExcelRequest extends ExcelConfiguration {
    private Long datasourceId;
    private Long copySourceId;
    private int editType;
}
