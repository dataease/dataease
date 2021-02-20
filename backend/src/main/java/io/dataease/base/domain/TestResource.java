package io.dataease.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestResource implements Serializable {
    private String id;

    private String testResourcePoolId;

    private String status;

    private Long createTime;

    private Long updateTime;

    private String configuration;

    private static final long serialVersionUID = 1L;
}
