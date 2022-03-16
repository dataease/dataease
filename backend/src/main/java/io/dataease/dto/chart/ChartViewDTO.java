package io.dataease.dto.chart;

import io.dataease.base.domain.ChartViewWithBLOBs;
import io.dataease.controller.request.chart.ChartExtFilterRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * @Author gin
 * @Date 2021/3/1 4:19 下午
 */
@Setter
@Getter
public class ChartViewDTO extends ChartViewWithBLOBs {
    @ApiModelProperty("数据")
    private Map<String, Object> data;
    @ApiModelProperty("权限")
    private String privileges;
    @ApiModelProperty("是否叶子节点")
    private Boolean isLeaf;
    @ApiModelProperty("父ID")
    private String pid;
    @ApiModelProperty("sql")
    private String sql;
    @ApiModelProperty("下钻")
    private boolean drill;
    @ApiModelProperty("下钻条件集合")
    private List<ChartExtFilterRequest> drillFilters;
    @ApiModelProperty("视图存放位置")
    private String position;
}
