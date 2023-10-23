package io.dataease.api.dataset.dto;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MultFieldValuesRequest {
    List<Long> fieldIds = new ArrayList<>();
    Long userId= null;

    private DeSortDTO sort;

}
