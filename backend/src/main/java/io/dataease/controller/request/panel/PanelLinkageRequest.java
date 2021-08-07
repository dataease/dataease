package io.dataease.controller.request.panel;

import io.dataease.dto.PanelViewLinkageDTO;

import java.util.List;
import java.util.Map;

/**
 * Author: wangjiahao
 * Date: 8/4/21
 * Description:
 */
public class PanelLinkageRequest {

    private String panelId;

    private String sourceViewId;

    private List<String> targetViewIds;

    private Map<String, PanelViewLinkageDTO> linkageInfo;

    public Map<String, PanelViewLinkageDTO> getLinkageInfo() {
        return linkageInfo;
    }

    public void setLinkageInfo(Map<String, PanelViewLinkageDTO> linkageInfo) {
        this.linkageInfo = linkageInfo;
    }

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
