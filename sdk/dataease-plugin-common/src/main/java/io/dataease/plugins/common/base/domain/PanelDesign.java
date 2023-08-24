package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class PanelDesign implements Serializable {
    private String id;

    private String panelId;

    private String componentId;

    private String componentStyle;

    private String componentPosition;

    private String componentType;

    private String componentDetails;

    private Long updateTime;

    private String updatePerson;

    private static final long serialVersionUID = 1L;
}