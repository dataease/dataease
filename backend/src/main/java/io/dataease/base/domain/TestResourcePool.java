package io.dataease.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestResourcePool implements Serializable {
    private String id;

    private String name;

    private String type;

    private String description;

    private String status;

    private Long createTime;

    private Long updateTime;

    private String image;

    private static final long serialVersionUID = 1L;
}
