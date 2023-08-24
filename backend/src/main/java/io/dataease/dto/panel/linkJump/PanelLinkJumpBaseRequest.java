package io.dataease.dto.panel.linkJump;

import lombok.Data;

/**
 * Author: wangjiahao
 * Date: 2021/10/28
 * Description:
 */
@Data
public class PanelLinkJumpBaseRequest {

    private String sourcePanelId;

    private String sourceViewId;

    private String sourceFieldId;

    private String targetPanelId;

    private String targetViewId;

    private String linkJumpId;

    public PanelLinkJumpBaseRequest() {
    }

    public PanelLinkJumpBaseRequest(String sourcePanelId, String sourceViewId, String targetPanelId, String targetViewId, String linkJumpId) {
        this.sourcePanelId = sourcePanelId;
        this.sourceViewId = sourceViewId;
        this.targetPanelId = targetPanelId;
        this.targetViewId = targetViewId;
        this.linkJumpId = linkJumpId;
    }
}

