package io.dataease.api.spreadsheet.dto;

import io.dataease.extensions.view.dto.ChartExtFilterDTO;
import io.dataease.extensions.view.dto.ChartViewFieldDTO;
import io.dataease.extensions.view.filter.FilterTreeObj;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PluginQueryDataConfig {
    private Long datasetId;
    private Map<String, List<ChartViewFieldDTO>> zones;
    private Long resultLimit;
    private FilterTreeObj customFilter;
    private List<ChartExtFilterDTO> queryFilter;
}
