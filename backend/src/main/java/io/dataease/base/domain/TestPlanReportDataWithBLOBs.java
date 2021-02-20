package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TestPlanReportDataWithBLOBs extends TestPlanReportData implements Serializable {
    private String executeResult;

    private String failurTestCases;

    private String moduleExecuteResult;

    private String apiCaseInfo;

    private String scenarioInfo;

    private String performanceInfo;

    private String issuesInfo;

    private static final long serialVersionUID = 1L;
}
