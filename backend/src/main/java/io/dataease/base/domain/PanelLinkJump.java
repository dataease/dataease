package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class PanelLinkJump implements Serializable {
    private String id;

    private String sourcePanelId;

    private String sourceViewId;

    private String linkJumpInfo;

    private Boolean checked;

    private static final long serialVersionUID = 1L;
}