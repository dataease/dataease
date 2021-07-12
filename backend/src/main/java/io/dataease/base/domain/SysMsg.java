package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class SysMsg implements Serializable {
    private Long msgId;

    private Long userId;

    private Long typeId;

    private Boolean status;

    private String param;

    private Long createTime;

    private Long readTime;

    private String content;

    private static final long serialVersionUID = 1L;
}