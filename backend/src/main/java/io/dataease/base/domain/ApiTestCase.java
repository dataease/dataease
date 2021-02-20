package io.dataease.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiTestCase implements Serializable {
    private String id;

    private String projectId;

    private String name;

    private String priority;

    private String apiDefinitionId;

    private String createUserId;

    private String updateUserId;

    private Long createTime;

    private Long updateTime;

    private Integer num;

    private String tags;

    private String lastResultId;

    private static final long serialVersionUID = 1L;
}
