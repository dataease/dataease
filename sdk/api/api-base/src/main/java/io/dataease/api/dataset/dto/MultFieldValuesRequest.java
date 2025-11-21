package io.dataease.api.dataset.dto;


import io.dataease.extensions.view.dto.ChartExtFilterDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MultFieldValuesRequest {
    List<Long> fieldIds = new ArrayList<>();
    Long userId = null;
    private List<ChartExtFilterDTO> filter;// 级联查询条件，多个条件之间用and拼接到SQL

    private DeSortDTO sort;
    private Integer resultMode = 0;

}
