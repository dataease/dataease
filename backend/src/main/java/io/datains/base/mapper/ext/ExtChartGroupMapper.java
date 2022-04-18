package io.datains.base.mapper.ext;

import io.datains.controller.request.chart.ChartGroupRequest;
import io.datains.dto.chart.ChartGroupDTO;

import java.util.List;

public interface ExtChartGroupMapper {
    List<ChartGroupDTO> search(ChartGroupRequest ChartGroup);
}
