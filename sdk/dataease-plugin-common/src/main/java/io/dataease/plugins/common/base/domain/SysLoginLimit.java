package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class SysLoginLimit implements Serializable {
    private Integer loginType;

    private String username;

    private Long recordTime;

    private static final long serialVersionUID = 1L;
}