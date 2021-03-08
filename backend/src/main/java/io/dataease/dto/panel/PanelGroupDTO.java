package io.dataease.dto.panel;

import io.dataease.base.domain.PanelGroup;
import io.dataease.dto.dataset.DataSetGroupDTO;

import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2021-03-05
 * Description:
 */
public class PanelGroupDTO extends PanelGroup {

    private String label;

    private List<PanelGroupDTO> children;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<PanelGroupDTO> getChildren() {
        return children;
    }

    public void setChildren(List<PanelGroupDTO> children) {
        this.children = children;
    }
}
