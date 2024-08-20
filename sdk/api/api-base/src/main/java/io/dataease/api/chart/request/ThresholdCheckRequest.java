package io.dataease.api.chart.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ThresholdCheckRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -8377694080272137660L;

    private Long chartId;

    private String thresholdRules;

    private String thresholdTemplate;
}
