package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class TestCaseReportTemplate implements Serializable {
    private String id;

    private String name;

    private String workspaceId;

    private String content;

    private static final long serialVersionUID = 1L;
}
