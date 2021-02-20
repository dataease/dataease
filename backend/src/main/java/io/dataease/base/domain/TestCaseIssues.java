package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class TestCaseIssues implements Serializable {
    private String id;

    private String testCaseId;

    private String issuesId;

    private static final long serialVersionUID = 1L;
}
