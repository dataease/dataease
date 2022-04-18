package io.datains.dto.panel;

import io.datains.base.domain.PanelDesign;
import io.datains.dto.chart.ChartViewDTO;
import lombok.Data;

/**
 * Author: wangjiahao
 * Date: 2021-03-17
 * Description:
 */
@Data
public class PanelDesignDTO extends PanelDesign {

    //当前视图是否在仪表板中显示
    private boolean keepFlag = false;

    //当前视图是否已经进行样式初始化
    private boolean styleInit = false;

    private ChartViewDTO chartView;

    private Object systemComponent;

    public PanelDesignDTO() {

    }
    public PanelDesignDTO(ChartViewDTO chartView) {
        this.chartView=chartView;
    }

}
