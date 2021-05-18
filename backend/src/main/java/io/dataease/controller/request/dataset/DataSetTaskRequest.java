package io.dataease.controller.request.dataset;

import io.dataease.base.domain.DatasetTableIncrementalConfig;
import io.dataease.base.domain.DatasetTableTask;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DataSetTaskRequest {
    private DatasetTableTask datasetTableTask;
    private DatasetTableIncrementalConfig datasetTableIncrementalConfig;
}
