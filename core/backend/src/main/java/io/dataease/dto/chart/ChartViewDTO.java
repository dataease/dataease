package io.dataease.dto.chart;

import com.google.gson.annotations.SerializedName;
import io.dataease.plugins.common.base.domain.ChartViewWithBLOBs;
import io.dataease.plugins.common.request.chart.ChartExtFilterRequest;
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
    @SerializedName("xaxis")
    private String xAxis;
    @SerializedName("xaxisExt")
    private String xAxisExt;
    @SerializedName("yaxis")
    private String yAxis;
    @SerializedName("yaxisExt")
    private String yAxisExt;

    private long totalPage;
    private long totalItems;
    private int datasetMode;
    private String datasourceType;
}
