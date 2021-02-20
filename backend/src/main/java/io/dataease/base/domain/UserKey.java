package io.dataease.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserKey implements Serializable {
    private String id;

    private String userId;

    private String accessKey;

    private String secretKey;

    private Long createTime;

    private String status;

    private static final long serialVersionUID = 1L;
}
