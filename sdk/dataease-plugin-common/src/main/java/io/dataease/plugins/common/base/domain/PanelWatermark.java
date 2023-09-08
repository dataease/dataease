package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class PanelWatermark implements Serializable {
    private String id;

    private String version;

    private String createBy;

    private Long createTime;

    private String settingContent;

    private static final long serialVersionUID = 1L;
}