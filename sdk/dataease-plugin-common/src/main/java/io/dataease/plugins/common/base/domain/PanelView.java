package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class PanelView implements Serializable {
    private String id;

    private String panelId;

    private String chartViewId;

    private String createBy;

    private Long createTime;

    private String updateBy;

    private Long updateTime;

    private String position;

    private String copyFromPanel;

    private String copyFromView;

    private String copyFrom;

    private String copyId;

    private byte[] content;

    private static final long serialVersionUID = 1L;
}