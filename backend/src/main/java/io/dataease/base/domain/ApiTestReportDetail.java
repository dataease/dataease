package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class ApiTestReportDetail implements Serializable {
    private String reportId;

    private String testId;

    private byte[] content;

    private static final long serialVersionUID = 1L;
}
