package io.dataease.extensions.view.dto;

import lombok.Data;

@Data
public class TableCalcTotalCfg {
    private String dataeaseName;
    private String aggregation;
    private String originName;
    private int extField;
    private long chartId;
    private long datasetGroupId;
}
