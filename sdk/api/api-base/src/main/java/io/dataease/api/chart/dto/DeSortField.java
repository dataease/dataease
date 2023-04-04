package io.dataease.api.chart.dto;

import io.dataease.api.dataset.dto.DatasetTableFieldDTO;
import lombok.Data;

@Data
public class DeSortField extends DatasetTableFieldDTO {

    private String orderDirection;
}
