package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class VAuthModel implements Serializable {
    private String id;

    private String name;

    private String label;

    private String pid;

    private String nodeType;

    private String modelType;

    private String modelInnerType;

    private String authType;

    private String createBy;

    private static final long serialVersionUID = 1L;
}