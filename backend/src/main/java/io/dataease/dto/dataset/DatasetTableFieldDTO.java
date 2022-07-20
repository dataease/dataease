package io.dataease.dto.dataset;

import io.dataease.plugins.common.base.domain.DatasetTableField;
import lombok.Data;

@Data
public class DatasetTableFieldDTO extends DatasetTableField {
    private String jsonPath;
}
