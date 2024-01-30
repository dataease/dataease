package io.dataease.plugins.common.dto.dataset.union;

import io.dataease.plugins.common.base.domain.DatasetTable;
import lombok.Data;

import java.util.List;

/**
 * @Author gin
 * @Date 2021/12/1 3:48 下午
 */
@Data
public class UnionDTO {
    private DatasetTable currentDs;
    private List<String> currentDsField;
    private List<UnionDTO> childrenDs;
    private UnionParamDTO unionToParent;
    private int allChildCount;
}
