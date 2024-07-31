package io.dataease.api.chart.request;

import io.dataease.extensions.view.dto.ChartViewDTO;
import lombok.Data;

import java.io.Serial;
import java.util.List;

/**
 * @Author wangjiahao
 */
@Data
public class ChartExcelRequest extends ChartExcelRequestInner {
    @Serial
    private static final long serialVersionUID = 3829386417457449431L;

    private String viewId;

    private String viewName;

    private ChartViewDTO viewInfo;

    private List<ChartExcelRequestInner> multiInfo;

    private boolean dataEaseBi = false;

}
