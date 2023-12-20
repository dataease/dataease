package io.dataease.plugins.view.entity.filter;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author Junjun
 */
@Data
public class PluginFilterTreeObj {
    @ApiModelProperty("逻辑关系")
    private String logic;
    @ApiModelProperty("逻辑树item")
    private List<PluginFilterTreeItem> items;
}
