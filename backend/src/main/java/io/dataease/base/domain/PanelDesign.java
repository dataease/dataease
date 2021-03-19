package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class PanelDesign implements Serializable {
    private String id;

    private String panelId;

    private String componentId;

    private String componentStyle;

    private String componentType;

    private String componentDetails;

    private Long createTime;

    private Long updateTime;

    private String createPersion;

    private String updatePersion;

    private static final long serialVersionUID = 1L;
}