package io.dataease.controller.response;

import io.dataease.plugins.common.base.domain.DatasetTableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class DatasetTableField4Type {
    @ApiModelProperty("维度")
    List<DatasetTableField> dimensionList;
    @ApiModelProperty("指标")
    List<DatasetTableField> quotaList;
}
