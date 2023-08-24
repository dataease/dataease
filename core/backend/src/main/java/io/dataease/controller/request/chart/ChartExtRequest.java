package io.dataease.controller.request.chart;

import io.dataease.dto.PermissionProxy;
import io.dataease.plugins.common.request.chart.ChartExtFilterRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author gin
 * @Date 2021/4/19 11:29 上午
 */
@Getter
@Setter
public class ChartExtRequest {
    @ApiModelProperty("视图额外过滤条件集合")
    private List<ChartExtFilterRequest> filter;

    @ApiModelProperty("联动过滤条件集合")
    private List<ChartExtFilterRequest> linkageFilters;

    @ApiModelProperty("外部参数过滤条件集合")
    private List<ChartExtFilterRequest> outerParamsFilters;

    @ApiModelProperty("下钻维度集合")
    private List<ChartDrillRequest> drill;

    @ApiModelProperty("数据查询来源")
    private String queryFrom;

    @ApiModelProperty("视图结果展示模式")
    private String resultMode;

    @ApiModelProperty("视图结果展示数量")
    private Integer resultCount;

    @ApiModelProperty("使用缓存:默认使用")
    private boolean cache = true;

    @ApiModelProperty("用户ID")
    private Long user = null;

    @ApiModelProperty(hidden = true)
    private PermissionProxy proxy;

    private Long goPage;

    private Long pageSize;

    private Boolean excelExportFlag = false;

}
