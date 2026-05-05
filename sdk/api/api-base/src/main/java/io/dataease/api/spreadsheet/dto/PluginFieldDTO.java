package io.dataease.api.spreadsheet.dto;

import io.dataease.extensions.view.dto.ChartViewFieldDTO;
import lombok.Data;

@Data
public class PluginFieldDTO extends ChartViewFieldDTO {
    private String displayName;
}
