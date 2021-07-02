package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class SysMsg implements Serializable {
    private Long msgId;

    private Long userId;

    private Integer type;

    private Boolean status;

    private String router;

    private String param;

    private Long createTime;

    private Long readTime;

    private String content;

    private static final long serialVersionUID = 1L;
}