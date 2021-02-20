package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class ApiDefinitionExecResult implements Serializable {
    private String id;

    private String name;

    private String resourceId;

    private String status;

    private String userId;

    private Long startTime;

    private Long endTime;

    private Long createTime;

    private String type;

    private String content;

    private static final long serialVersionUID = 1L;
}
