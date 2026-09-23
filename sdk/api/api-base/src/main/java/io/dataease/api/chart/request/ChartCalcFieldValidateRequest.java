package io.dataease.api.chart.request;

import io.dataease.extensions.datasource.dto.CalParam;
import lombok.Data;

import java.util.List;

@Data
public class ChartCalcFieldValidateRequest {
    private Long datasetId;

    private Long chartId;

    // 与图表计算字段一致，表达式使用 Base64 传输。
    private String originName;

    private List<CalParam> params;
}
