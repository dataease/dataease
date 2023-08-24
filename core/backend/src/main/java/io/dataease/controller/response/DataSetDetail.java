package io.dataease.controller.response;

import io.dataease.dto.dataset.DataSetTableDTO;
import io.dataease.plugins.common.base.domain.DatasetTable;
import io.dataease.plugins.common.base.domain.Datasource;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DataSetDetail {
    @ApiModelProperty("数据集")
    private DataSetTableDTO table;
    @ApiModelProperty("数据源")
    private Datasource datasource;
}
