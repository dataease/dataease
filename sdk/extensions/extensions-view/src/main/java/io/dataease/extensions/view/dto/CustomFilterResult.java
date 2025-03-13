package io.dataease.extensions.view.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomFilterResult {
    private List<ChartExtFilterDTO> filterList;
    private Map<String, Object> context;
}
