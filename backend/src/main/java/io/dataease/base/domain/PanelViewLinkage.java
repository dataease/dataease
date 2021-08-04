package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class PanelViewLinkage implements Serializable {
    private String id;

    private String panelId;

    private String sourceViewId;

    private String targetViewId;

    private Long updateTime;

    private String updatePeople;

    private String ext1;

    private String ext2;

    private static final long serialVersionUID = 1L;
}