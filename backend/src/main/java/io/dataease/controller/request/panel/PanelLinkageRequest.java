package io.dataease.controller.request.panel;

import io.dataease.dto.PanelViewLinkageDTO;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Map;

/**
 * Author: wangjiahao
 * Date: 8/4/21
 * Description:
 */
public class PanelLinkageRequest {
    @ApiModelProperty("仪表板ID")
    private String panelId;
    @ApiModelProperty("源视图ID")
    private String sourceViewId;
    @ApiModelProperty("目标视图ID集合")
    private List<String> targetViewIds;

    @ApiModelProperty("联动信息")
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
