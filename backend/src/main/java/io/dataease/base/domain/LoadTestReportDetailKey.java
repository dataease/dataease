package io.dataease.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoadTestReportDetailKey implements Serializable {
    private String reportId;

    private Long part;

    private static final long serialVersionUID = 1L;
}
