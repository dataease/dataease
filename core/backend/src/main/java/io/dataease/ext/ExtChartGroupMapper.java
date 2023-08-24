package io.dataease.ext;

import io.dataease.controller.request.chart.ChartGroupRequest;
import io.dataease.dto.chart.ChartGroupDTO;

import java.util.List;

public interface ExtChartGroupMapper {
    List<ChartGroupDTO> search(ChartGroupRequest ChartGroup);
}
