package io.dataease.api.chart.dto;

import io.dataease.dto.dataset.DatasetTableFieldDTO;
import lombok.Data;

@Data
public class DeSortField extends DatasetTableFieldDTO {

    private String orderDirection;
}
