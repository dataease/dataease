package io.dataease.dto.panel.po;

import io.dataease.base.domain.PanelView;

/**
 * Author: wangjiahao
 * Date: 2021-07-06
 * Description:
 */
public class PanelViewInsertDTO extends PanelView {

    public PanelViewInsertDTO() {
    }

    public PanelViewInsertDTO(String chartViewId,String panelGroupId) {
        super();
        super.setChartViewId(chartViewId);
        super.setPanelId(panelGroupId);
    }

}
