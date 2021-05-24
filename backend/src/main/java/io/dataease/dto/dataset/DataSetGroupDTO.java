package io.dataease.dto.dataset;

import io.dataease.base.domain.DatasetGroup;
import io.dataease.commons.model.ITreeBase;
import lombok.Data;

import java.util.List;

/**
 * @Author gin
 * @Date 2021/2/20 8:17 下午
 */
@Data
public class DataSetGroupDTO extends DatasetGroup implements ITreeBase<DataSetGroupDTO> {
    private String label;
    private List<DataSetGroupDTO> children;

    private String privileges;
}
