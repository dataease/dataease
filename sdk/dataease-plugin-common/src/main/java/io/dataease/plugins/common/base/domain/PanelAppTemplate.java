package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class PanelAppTemplate implements Serializable {
    private String id;

    private String name;

    private String nodeType;

    private Integer level;

    private String pid;

    private String version;

    private Long updateTime;

    private String updateUser;

    private Long createTime;

    private String createUser;

    private static final long serialVersionUID = 1L;
}