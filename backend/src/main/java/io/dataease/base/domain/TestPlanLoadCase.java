package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class TestPlanLoadCase implements Serializable {
    private String id;

    private String testPlanId;

    private String loadCaseId;

    private String loadReportId;

    private String status;

    private Long createTime;

    private Long updateTime;

    private static final long serialVersionUID = 1L;
}
