package io.dataease.dto.dataset;

import io.dataease.base.domain.DatasetTableTask;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author gin
 * @Date 2021/3/9 3:19 下午
 */
@Getter
@Setter
public class DataSetTaskDTO extends DatasetTableTask {
    private String datasetName;
    private Long nextExecTime;
    private String taskStatus;
    private String msg;
    private String privileges;
}
