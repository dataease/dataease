package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class ApiTestEnvironment implements Serializable {
    private String id;

    private String name;

    private String projectId;

    private String protocol;

    private String socket;

    private String domain;

    private Integer port;

    private static final long serialVersionUID = 1L;
}
