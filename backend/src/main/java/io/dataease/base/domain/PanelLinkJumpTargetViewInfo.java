package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class PanelLinkJumpTargetViewInfo implements Serializable {
    private String targetId;

    private String linkJumpInfoId;

    private String targetViewId;

    private String targetFieldId;

    private static final long serialVersionUID = 1L;
}