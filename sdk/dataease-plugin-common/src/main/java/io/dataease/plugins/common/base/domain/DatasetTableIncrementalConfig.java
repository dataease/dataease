package io.dataease.plugins.common.base.domain;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DatasetTableIncrementalConfig implements Serializable {
    @ApiModelProperty("ID")
    private String id;
    @ApiModelProperty("表ID")
    private String tableId;
    @ApiModelProperty("删除增量")
    private String incrementalDelete;
    @ApiModelProperty("新增增量")
    private String incrementalAdd;

    private static final long serialVersionUID = 1L;
}