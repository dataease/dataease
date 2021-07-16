package io.dataease.base.domain;

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

    private byte[] content;

    private static final long serialVersionUID = 1L;
}