package io.dataease.api.dataset.union;

import io.dataease.api.dataset.dto.DatasetTableDTO;
import lombok.Data;

import java.util.List;

/**
 * @Author gin
 */
@Data
public class UnionDTO {
    private DatasetTableDTO currentDs;
    private List<String> currentDsField;
    private List<UnionDTO> childrenDs;
    private UnionParamDTO unionToParent;
    private int allChildCount;
}
