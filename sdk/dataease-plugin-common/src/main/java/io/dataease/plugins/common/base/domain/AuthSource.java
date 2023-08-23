package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class AuthSource implements Serializable {
    private String id;

    private String status;

    private Long createTime;

    private Long updateTime;

    private String description;

    private String name;

    private String type;

    private String configuration;

    private static final long serialVersionUID = 1L;
}
