package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class DeDriver implements Serializable {
    private String id;

    private String name;

    private Long createTime;

    private String type;

    private String driverClass;

    private String desc;

    private String surpportVersions;

    private static final long serialVersionUID = 1L;
}