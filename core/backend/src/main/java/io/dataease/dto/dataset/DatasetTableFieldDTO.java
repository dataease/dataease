package io.dataease.dto.dataset;

import io.dataease.plugins.common.base.domain.DatasetTableField;
import lombok.Data;

import java.util.List;

@Data
public class DatasetTableFieldDTO extends DatasetTableField {
    private String jsonPath;
    private List<Object> deTypeCascader;
}
