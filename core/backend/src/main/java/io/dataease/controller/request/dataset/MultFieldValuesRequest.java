package io.dataease.controller.request.dataset;

import io.dataease.dto.dataset.DeSortDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MultFieldValuesRequest {
    List<String> fieldIds = new ArrayList<>();
    Long userId= null;

    private DeSortDTO sort;

}
