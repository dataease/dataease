package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class TestPlanReportData implements Serializable {
    private String id;

    private String testPlanReportId;

    private static final long serialVersionUID = 1L;
}
