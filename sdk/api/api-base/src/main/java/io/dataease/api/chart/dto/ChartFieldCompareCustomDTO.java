package io.dataease.api.chart.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChartFieldCompareCustomDTO {
    private String field;
    private String calcType;
    private String timeType;
    private String currentTime;
    private String compareTime;
    private List<String> currentTimeRange;
    private List<String> compareTimeRange;

}
