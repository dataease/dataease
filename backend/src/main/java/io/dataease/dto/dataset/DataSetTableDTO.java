package io.dataease.dto.dataset;

import io.dataease.base.domain.DatasetTable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author gin
 * @Date 2021/2/23 2:55 下午
 */
@Setter
@Getter
public class DataSetTableDTO extends DatasetTable {
    private List<DataSetTableDTO> children;
    private String privileges;

    private Boolean isLeaf;
    private String pid;
}
