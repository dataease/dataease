package io.dataease.api.spreadsheet.vo;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.dataease.extensions.view.dto.ChartViewFieldDTO;
import lombok.Data;

import java.util.List;

@Data
public class PluginQueryDataResponse {
    private List<? extends ChartViewFieldDTO> fields;
    private List<ObjectNode> rowData;
    private Long total;
}
