package io.dataease.dto.panel;

import io.dataease.base.domain.ChartViewWithBLOBs;
import io.dataease.base.domain.DatasetTableField;
import io.dataease.base.domain.PanelGroupWithBLOBs;
import io.dataease.commons.model.ITreeBase;
import io.dataease.datasource.dto.TableFiled;
import io.dataease.dto.chart.ChartViewDTO;
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
