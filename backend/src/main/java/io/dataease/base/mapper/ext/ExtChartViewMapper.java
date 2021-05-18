package io.dataease.base.mapper.ext;

import io.dataease.controller.request.chart.ChartViewRequest;
import io.dataease.dto.chart.ChartViewDTO;

import java.util.List;

public interface ExtChartViewMapper {
    List<ChartViewDTO> search(ChartViewRequest request);
}
