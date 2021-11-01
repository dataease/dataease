package io.dataease.dto.datasource;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TableFiled {
    @ApiModelProperty("字段名称")
    private String fieldName;
    @ApiModelProperty("重新标记")
    private String remarks;
    @ApiModelProperty("字段类型")
    private String fieldType;
    @ApiModelProperty("字段大小")
    private int fieldSize;

}
