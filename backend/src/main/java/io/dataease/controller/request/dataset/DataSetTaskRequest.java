package io.dataease.controller.request.dataset;

import io.dataease.plugins.common.base.domain.DatasetTableIncrementalConfig;
import io.dataease.plugins.common.base.domain.DatasetTableTask;
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
