package io.dataease.plugins.view.official.dto;

import java.util.Map;

import io.dataease.plugins.common.dto.chart.AxisChartDataAntVDTO;
import lombok.Data;

@Data
public class SymbolMapResultDTO extends AxisChartDataAntVDTO {

    private String longitude;

    private String latitude;

    private String color;

    private Object busiValue;

    private Map<String, Object> properties;
}
