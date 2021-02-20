package io.dataease.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoadTestReportLog implements Serializable {
    private String id;

    private String reportId;

    private String resourceId;

    private Long part;

    private String content;

    private static final long serialVersionUID = 1L;
}
