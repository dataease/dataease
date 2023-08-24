package io.dataease.controller.request.chart;

import io.dataease.plugins.common.base.domain.ChartGroup;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Set;


@Data
public class ChartGroupRequest extends ChartGroup {
    @ApiModelProperty("排序")
    private String sort;
    @ApiModelProperty("用户ID")
    private String userId;
    @ApiModelProperty("ID集合")
    private Set<String> ids;
}
