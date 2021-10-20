package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class PanelLink implements Serializable {
    private String resourceId;

    private Boolean valid;

    private Boolean enablePwd;

    private String pwd;

    private Long overTime;

    private static final long serialVersionUID = 1L;
}