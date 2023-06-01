package io.dataease.api.chart.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.dataease.api.chart.request.ChartExtRequest;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Author gin
 */
@Data
public class ChartViewDTO extends ChartViewBaseDTO {
    private Map<String, Object> data;
    private String privileges;
    private Boolean isLeaf;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pid;
    private String sql;
    private boolean drill;
    private List<ChartExtFilterDTO> drillFilters;
    private String position;

    private long totalPage;
    private long totalItems;
    private int datasetMode;
    private String datasourceType;

    private ChartExtRequest chartExtRequest;
    private boolean cache;
}
