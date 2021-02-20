package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class ApiScenarioReportDetail implements Serializable {
    private String reportId;

    private String projectId;

    private byte[] content;

    private static final long serialVersionUID = 1L;
}
