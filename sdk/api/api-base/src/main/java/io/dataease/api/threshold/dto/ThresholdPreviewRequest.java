package io.dataease.api.threshold.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ThresholdPreviewRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 3609209252630995739L;

    private Long chartId;

    private String thresholdRules;

    private String msgContent;
}
