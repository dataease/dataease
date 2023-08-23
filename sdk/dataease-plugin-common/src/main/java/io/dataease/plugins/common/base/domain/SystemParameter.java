package io.dataease.plugins.common.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class SystemParameter implements Serializable {
    private String paramKey;

    private String paramValue;

    private String type;

    private Integer sort;

    private static final long serialVersionUID = 1L;
}
