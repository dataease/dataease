package io.dataease.dto.datasource;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TableDesc {
    @ApiModelProperty("表名称")
    private String name;
    @ApiModelProperty("表备注")
    private String remark;
}
