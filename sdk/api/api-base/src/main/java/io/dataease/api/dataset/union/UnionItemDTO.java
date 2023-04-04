package io.dataease.api.dataset.union;

import io.dataease.api.dataset.dto.DatasetTableFieldDTO;
import lombok.Data;

/**
 * @Author gin
 */
@Data
public class UnionItemDTO {
    private DatasetTableFieldDTO parentField;
    private DatasetTableFieldDTO currentField;
}
