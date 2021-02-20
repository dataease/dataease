package io.dataease.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoadTestReportResult implements Serializable {
    private String id;

    private String reportId;

    private String reportKey;

    private String reportValue;

    private static final long serialVersionUID = 1L;
}
