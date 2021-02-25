package io.dataease.dto.chart;

import io.dataease.base.domain.DatasetGroup;
import lombok.Data;

import java.util.List;


@Data
public class ChartGroupDTO extends DatasetGroup {
    private String label;
    private List<ChartGroupDTO> children;
}
