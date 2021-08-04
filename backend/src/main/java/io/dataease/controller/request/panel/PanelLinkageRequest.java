package io.dataease.controller.request.panel;

import java.util.List;

/**
 * Author: wangjiahao
 * Date: 8/4/21
 * Description:
 */
public class PanelLinkageRequest {

    private String panelId;

    private String sourceViewId;

    private List<String> targetViewIds;

    public String getPanelId() {
        return panelId;
    }

    public void setPanelId(String panelId) {
        this.panelId = panelId;
    }

    public String getSourceViewId() {
        return sourceViewId;
    }

    public void setSourceViewId(String sourceViewId) {
        this.sourceViewId = sourceViewId;
    }

    public List<String> getTargetViewIds() {
        return targetViewIds;
    }

    public void setTargetViewIds(List<String> targetViewIds) {
        this.targetViewIds = targetViewIds;
    }
}
