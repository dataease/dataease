package io.datains.controller.request.dataset;

import io.datains.base.domain.DatasetTableIncrementalConfig;
import io.datains.base.domain.DatasetTableTask;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DataSetTaskRequest {
    @ApiModelProperty("数据集任务")
    private DatasetTableTask datasetTableTask;
    @ApiModelProperty("数据集增量配置")
    private DatasetTableIncrementalConfig datasetTableIncrementalConfig;
}
