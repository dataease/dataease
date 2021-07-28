package io.dataease.dto.dataset;

import io.dataease.base.domain.DatasetTableTaskLog;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author gin
 * @Date 2021/3/9 3:19 下午
 */
@Getter
@Setter
public class DataSetTaskLogDTO extends DatasetTableTaskLog {
    private String name;
    private String datasetName;
}
