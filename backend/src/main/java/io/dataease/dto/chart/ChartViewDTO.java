package io.dataease.dto.chart;

import io.dataease.base.domain.ChartViewWithBLOBs;
import io.dataease.controller.request.chart.ChartExtFilterRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * @Author gin
 * @Date 2021/3/1 4:19 下午
 */
@Setter
@Getter
public class ChartViewDTO extends ChartViewWithBLOBs {
    private Map<String, Object> data;

    private String privileges;

    private Boolean isLeaf;
    private String pid;
    private String sql;

    private boolean drill;

    private List<ChartExtFilterRequest> drillFilters;
}
