package io.dataease.dto.chart;

import io.dataease.base.domain.DatasetGroup;
import io.dataease.commons.model.ITreeBase;
import lombok.Data;

import java.util.List;


@Data
public class ChartGroupDTO extends DatasetGroup implements ITreeBase<ChartGroupDTO> {
    private String label;

    private List<ChartGroupDTO> children;

    private String privileges;
}
