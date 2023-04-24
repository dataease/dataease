package io.dataease.api.chart.dto;

import lombok.Data;

@Data
public class ChartFieldCompareDTO {
    private String type;
    private String resultData;
    private Long field;
    private ChartFieldCompareCustomDTO custom;
}
