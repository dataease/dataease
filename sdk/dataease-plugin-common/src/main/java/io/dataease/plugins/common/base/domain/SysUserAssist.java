package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class SysUserAssist implements Serializable {
    private Long userId;

    private Boolean needFirstNoti;

    private String wecomId;

    private String dingtalkId;

    private String larkId;

    private String larksuiteId;

    private static final long serialVersionUID = 1L;
}