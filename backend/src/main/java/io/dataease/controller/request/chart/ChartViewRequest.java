package io.dataease.controller.request.chart;

import io.dataease.base.domain.ChartViewWithBLOBs;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author gin
 * @Date 2021/3/1 1:32 下午
 */
@Setter
@Getter
public class ChartViewRequest extends ChartViewWithBLOBs {
    @ApiModelProperty("排序")
    private String sort;
    @ApiModelProperty("当前登陆用户ID")
    private String userId;
    @ApiModelProperty("编辑来源")
    private String editFrom;
    @ApiModelProperty("查询来源")
    private String queryFrom;
}
