package io.dataease.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRole implements Serializable {
    private String id;

    private String userId;

    private String roleId;

    private String sourceId;

    private Long createTime;

    private Long updateTime;

    private static final long serialVersionUID = 1L;
}
