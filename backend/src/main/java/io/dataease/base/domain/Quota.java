package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class Quota implements Serializable {
    private String id;

    private Integer api;

    private Integer performance;

    private Integer maxThreads;

    private Integer duration;

    private String resourcePool;

    private String organizationId;

    private String workspaceId;

    private Boolean useDefault;

    private Long updateTime;

    private static final long serialVersionUID = 1L;
}
