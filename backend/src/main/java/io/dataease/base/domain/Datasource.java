package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class Datasource implements Serializable {
    private String id;

    private String name;

    private String desc;

    private String type;

    private Long createTime;

    private Long updateTime;

    private String configuration;

    private static final long serialVersionUID = 1L;
}