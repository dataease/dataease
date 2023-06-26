package io.dataease.api.dataset.vo;

import io.dataease.api.dataset.dto.DatasetNodeDTO;
import io.dataease.model.ITreeBase;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DatasetTreeNodeVO extends DatasetNodeDTO implements Serializable, ITreeBase<DatasetTreeNodeVO> {

    private List<DatasetTreeNodeVO> children;

    private Boolean leaf;

}
