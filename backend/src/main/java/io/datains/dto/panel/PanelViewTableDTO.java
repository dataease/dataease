package io.datains.dto.panel;

import io.datains.base.domain.ChartViewWithBLOBs;
import io.datains.base.domain.DatasetTableField;
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

    private List<DatasetTableField> tableFields;


}
