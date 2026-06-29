package io.dataease.api.spreadsheet.vo;

import io.dataease.extensions.view.dto.ChartViewFieldDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SummaryQueryDataResponse extends PluginQueryDataResponse {

    private List<? extends ChartViewFieldDTO> rowFields;

    private List<? extends ChartViewFieldDTO> columnFields;

    private List<? extends ChartViewFieldDTO> quotaFields;
}
