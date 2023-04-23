package io.dataease.api.chart.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Author gin
 * @Date 2021/3/1 4:19 下午
 */
@Data
public class ChartViewDTO extends ChartViewBaseDTO {
    private Map<String, Object> data;
    private String privileges;
    private Boolean isLeaf;
    private String pid;
    private String sql;
    private boolean drill;
    private List<ChartExtFilterDTO> drillFilters;
    private String position;

    private long totalPage;
    private long totalItems;
    private int datasetMode;
    private String datasourceType;
}
