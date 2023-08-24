package io.dataease.plugins.common.dto.chart;

import lombok.Data;

/**
 * @Author gin
 * @Date 2021/12/9 2:48 下午
 */
@Data
public class ChartFieldCompareDTO {
    private String type;
    private String resultData;
    private String field;
    private ChartFieldCompareCustomDTO custom;
}
