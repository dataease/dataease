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

    private Boolean leaf;

    private String privileges;

}
