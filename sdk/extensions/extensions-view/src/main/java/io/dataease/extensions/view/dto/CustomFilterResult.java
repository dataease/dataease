package io.dataease.extensions.view.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class CustomFilterResult {
    private List<ChartExtFilterDTO> filterList;
    private Map<String, Object> context;
}
