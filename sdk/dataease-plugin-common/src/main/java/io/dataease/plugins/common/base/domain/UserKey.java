package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class UserKey implements Serializable {
    private Long id;

    private Long userId;

    private String accessKey;

    private String secretKey;

    private Long createTime;

    private String status;

    private static final long serialVersionUID = 1L;
}