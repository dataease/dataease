package io.dataease.api.visualization.request;

import lombok.Data;

/**
 * @author : WangJiaHao
 * @date : 2023/7/18 14:26
 */
@Data
public class VisualizationLinkJumpBaseRequest {

    private String sourceDvId;

    private String sourceViewId;

    private String sourceFieldId;

    private String targetDvId;

    private String targetViewId;

    private String linkJumpId;

    private Boolean activeStatus;

    public VisualizationLinkJumpBaseRequest() {
    }

    public VisualizationLinkJumpBaseRequest(String sourceDvId, String sourceViewId, String targetDvId, String targetViewId, String linkJumpId) {
        this.sourceDvId = sourceDvId;
        this.sourceViewId = sourceViewId;
        this.targetDvId = targetDvId;
        this.targetViewId = targetViewId;
        this.linkJumpId = linkJumpId;
    }
}
