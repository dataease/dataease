package io.dataease.plugins.common.request.chart.filter;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;


/**
 * @Author Junjun
 */
@Data
public class FilterTreeObj {
    @ApiModelProperty("逻辑关系")
    private String logic;
    @ApiModelProperty("逻辑树item")
    private List<FilterTreeItem> items;
}
