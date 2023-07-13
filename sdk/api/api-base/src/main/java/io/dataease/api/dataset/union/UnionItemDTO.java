package io.dataease.api.dataset.union;

import io.dataease.dto.dataset.DatasetTableFieldDTO;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author gin
 */
@Data
public class UnionItemDTO implements Serializable {
    private DatasetTableFieldDTO parentField;
    private DatasetTableFieldDTO currentField;
}
