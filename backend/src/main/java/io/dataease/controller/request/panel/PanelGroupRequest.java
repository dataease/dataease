package io.dataease.controller.request.panel;

import io.dataease.base.domain.PanelGroup;
import io.dataease.dto.panel.PanelGroupDTO;
import lombok.Data;

/**
 * Author: wangjiahao
 * Date: 2021-03-05
 * Description:
 */
@Data
public class PanelGroupRequest extends PanelGroupDTO {
    private String sort;

    public PanelGroupRequest() {
    }

    public PanelGroupRequest(String pid) {
        super.setPid(pid);
    }
}
