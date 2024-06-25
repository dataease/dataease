package io.dataease.extensions.view.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class AxisFormatResult {
    private Map<ChartAxis, List<ChartViewFieldDTO>> axisMap;
    private Map<String, Object> context;
}
