package io.dataease.api.chart.dto;

import lombok.Data;

@Data
public class ChartFieldCompareDTO {
    private String type;
    private String resultData;
    private String field;
    private ChartFieldCompareCustomDTO custom;
}
