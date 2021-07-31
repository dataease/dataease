package io.dataease.controller.request.chart;

import io.dataease.base.domain.ChartGroup;
import lombok.Data;

import java.util.Set;


@Data
public class ChartGroupRequest extends ChartGroup {
    private String sort;
    private String userId;
    private Set<String> ids;
}
