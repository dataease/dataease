package io.dataease.dto.panel;

import io.dataease.base.domain.PanelGroup;
import io.dataease.base.domain.PanelGroupWithBLOBs;
import io.dataease.dto.chart.ChartViewDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2021-03-05
 * Description:
 */
@Data
public class PanelGroupDTO extends PanelGroupWithBLOBs {

    private String label;

    private List<PanelGroupDTO> children;

    //仪表盘组件样式设计
    private List<PanelDesignDTO> panelDesigns = new ArrayList<>();

    private List<ChartViewDTO> viewsUsable = new ArrayList<>();

}
