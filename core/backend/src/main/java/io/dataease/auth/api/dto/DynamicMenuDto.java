package io.dataease.auth.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


import java.io.Serializable;
import java.util.List;

@Data
public class DynamicMenuDto implements Serializable {

    @ApiModelProperty("路径")
    private String path;
    @ApiModelProperty("组件")
    private String component;
    @ApiModelProperty("重定向")
    private String redirect;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("元数据")
    private MenuMeta meta;
    @ApiModelProperty("父ID")
    private Long pid;
    @ApiModelProperty("ID")
    private Long id;
    @ApiModelProperty("权限")
    private String permission;
    @ApiModelProperty("是否隐藏")
    private Boolean hidden;
    @ApiModelProperty("类型")
    private Integer type;
    @ApiModelProperty("菜单序号")
    private Integer menuSort;
    @ApiModelProperty("是否插件")
    private Boolean isPlugin;
    @ApiModelProperty(hidden = true)
    private Boolean noLayout;
    @ApiModelProperty("子节点")
    private List<DynamicMenuDto> children;

}
