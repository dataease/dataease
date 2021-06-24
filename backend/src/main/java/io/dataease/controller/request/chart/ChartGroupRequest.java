package io.dataease.controller.request.chart;

import io.dataease.base.domain.ChartGroup;
import lombok.Data;


@Data
public class ChartGroupRequest extends ChartGroup {
    private String sort;
    private String userId;
}
