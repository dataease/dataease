package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class PanelLinkJumpTargetViewInfo implements Serializable {
    private String targetId;

    private String linkJumpInfoId;

    private String targetViewId;

    private String targetFieldId;

    private String copyFrom;

    private String copyId;

    private static final long serialVersionUID = 1L;
}