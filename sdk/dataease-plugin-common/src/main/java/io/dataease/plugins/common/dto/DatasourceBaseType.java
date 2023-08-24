package io.dataease.plugins.common.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Author: wangjiahao
 * Date: 2022/7/27
 * Description:
 */
@Data
public class DatasourceBaseType {
    @ApiModelProperty("数据源类型")
    private String type;
    @ApiModelProperty("数据源名称")
    private String name;

    public DatasourceBaseType() {
    }

    public DatasourceBaseType(String type, String name) {
        this.type = type;
        this.name = name;
    }
}
