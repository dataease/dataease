package io.dataease.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiScenarioReport implements Serializable {
    private String id;

    private String projectId;

    private String name;

    private Long createTime;

    private Long updateTime;

    private String status;

    private String userId;

    private String triggerMode;

    private String executeType;

    private String scenarioName;

    private String scenarioId;

    private String description;

    private static final long serialVersionUID = 1L;
}
