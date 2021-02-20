package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class ApiScenario implements Serializable {
    private String id;

    private String projectId;

    private String tags;

    private String userId;

    private String apiScenarioModuleId;

    private String modulePath;

    private String name;

    private String level;

    private String status;

    private String principal;

    private Integer stepTotal;

    private String followPeople;

    private String schedule;

    private Long createTime;

    private Long updateTime;

    private String passRate;

    private String lastResult;

    private String reportId;

    private Integer num;

    private static final long serialVersionUID = 1L;
}
