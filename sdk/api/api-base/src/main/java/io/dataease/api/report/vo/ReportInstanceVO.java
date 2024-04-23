package io.dataease.api.report.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ReportInstanceVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -7845204306008848053L;

    private Long id;

    private String name;

    private Long startTime;

    private Integer execStatus;
}
