package io.dataease.controller.response;

import io.dataease.plugins.common.base.domain.ChartViewField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class ChartViewField4Type {
    @ApiModelProperty("维度")
    List<ChartViewField> dimensionList;
    @ApiModelProperty("指标")
    List<ChartViewField> quotaList;
}
