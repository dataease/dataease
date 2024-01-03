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
public class PanelViewTableDTO extends ChartViewWithBLOBs {
    @ApiModelProperty("仪表板ID")
    private String panelId;

    @ApiModelProperty("仪表板数据")
    private String basePanelData;

    private List<DatasetTableField> tableFields;


}
