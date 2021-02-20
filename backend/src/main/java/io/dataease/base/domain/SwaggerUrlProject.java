package io.dataease.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class SwaggerUrlProject implements Serializable {
    private String id;

    private String projectId;

    private String swaggerUrl;

    private String moduleId;

    private String modulePath;

    private String modeId;

    private static final long serialVersionUID = 1L;
}
