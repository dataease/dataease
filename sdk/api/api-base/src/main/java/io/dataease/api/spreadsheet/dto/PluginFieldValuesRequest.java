package io.dataease.api.spreadsheet.dto;

import io.dataease.extensions.view.dto.ChartViewFieldDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class PluginFieldValuesRequest extends PluginQueryRequest {
    private ChartViewFieldDTO field;
}
