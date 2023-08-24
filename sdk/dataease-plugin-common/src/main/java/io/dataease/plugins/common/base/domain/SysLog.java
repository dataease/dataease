package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class SysLog implements Serializable {
    private Long id;

    private Long userId;

    private String loginName;

    private String nickName;

    private String ip;

    private Integer sourceType;

    private String sourceId;

    private String sourceName;

    private Integer operateType;

    private Long time;

    private static final long serialVersionUID = 1L;
}