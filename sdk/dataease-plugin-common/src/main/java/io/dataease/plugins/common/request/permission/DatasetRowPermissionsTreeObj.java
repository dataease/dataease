package io.dataease.plugins.common.request.permission;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DatasetRowPermissionsTreeObj implements Serializable {
    @ApiModelProperty("逻辑关系")
    private String logic;
    @ApiModelProperty("权限树item")
    private List<DatasetRowPermissionsTreeItem> items;

    private static final long serialVersionUID = 1L;
}
