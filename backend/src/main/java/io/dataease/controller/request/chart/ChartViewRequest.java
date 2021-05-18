package io.dataease.controller.request.chart;

import io.dataease.base.domain.ChartViewWithBLOBs;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author gin
 * @Date 2021/3/1 1:32 下午
 */
@Setter
@Getter
public class ChartViewRequest extends ChartViewWithBLOBs {
    private String sort;

    private String userId;
}
