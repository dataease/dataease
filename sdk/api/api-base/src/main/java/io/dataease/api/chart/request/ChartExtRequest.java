package io.dataease.api.chart.request;

import io.dataease.api.chart.dto.ChartExtFilterDTO;
import io.dataease.api.chart.dto.PermissionProxy;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author gin
 */
@Data
public class ChartExtRequest {
    @ApiModelProperty("视图额外过滤条件集合")
    private List<ChartExtFilterDTO> filter;

    @ApiModelProperty("联动过滤条件集合")
    private List<ChartExtFilterDTO> linkageFilters;

    @ApiModelProperty("外部参数过滤条件集合")
    private List<ChartExtFilterDTO> outerParamsFilters;

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
