package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class PanelLinkJumpInfo implements Serializable {
    private String id;

    private String linkJumpId;

    private String linkType;

    private String jumpType;

    private String targetPanelId;

    private String sourceFieldId;

    private String content;

    private Boolean checked;

    private Boolean attachParams;

    private String copyFrom;

    private String copyId;

    private static final long serialVersionUID = 1L;
}