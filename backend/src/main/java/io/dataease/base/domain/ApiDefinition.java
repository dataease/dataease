package io.dataease.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiDefinition implements Serializable {
    private String id;

    private String projectId;

    private String name;

    private String method;

    private String modulePath;

    private String environmentId;

    private String schedule;

    private String status;

    private String moduleId;

    private String userId;

    private Long createTime;

    private Long updateTime;

    private String protocol;

    private String path;

    private Integer num;

    private String tags;

    private static final long serialVersionUID = 1L;
}
