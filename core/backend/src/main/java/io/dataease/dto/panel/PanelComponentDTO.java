package io.dataease.dto.panel;

import io.dataease.plugins.common.base.domain.ChartViewWithBLOBs;
import io.dataease.plugins.common.base.domain.DatasetTableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2021-03-05
 * Description:
 */
@Data
public class PanelComponentDTO {

    private String bashComponentData;

    private List<PanelViewTableDTO> panelViewTables;

    public PanelComponentDTO(String bashComponentData, List<PanelViewTableDTO> panelViewTables) {
        this.bashComponentData = bashComponentData;
        this.panelViewTables = panelViewTables;
    }
}
