package io.datains.controller.response;

import io.datains.base.domain.DatasetTable;
import io.datains.base.domain.Datasource;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DataSetDetail {
    @ApiModelProperty("数据集")
    private DatasetTable table;
    @ApiModelProperty("数据源")
    private Datasource datasource;
}
