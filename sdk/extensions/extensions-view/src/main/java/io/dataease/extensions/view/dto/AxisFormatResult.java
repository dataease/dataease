package io.dataease.extensions.view.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AxisFormatResult {
    private Map<ChartAxis, List<ChartViewFieldDTO>> axisMap;
    private Map<String, Object> context;
}
