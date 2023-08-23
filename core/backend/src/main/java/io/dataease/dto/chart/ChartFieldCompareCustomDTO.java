package io.dataease.dto.chart;

import lombok.Data;

import java.util.List;

/**
 * @Author gin
 * @Date 2021/12/9 2:48 下午
 */
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
