package io.dataease.controller.sys.base;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class ConditionEntity implements Serializable {

    @ApiModelProperty(value = "字段")
    private String field;

    @ApiModelProperty(value = "操作符")
    private String operator;

    @ApiModelProperty(value = "字段值")
    private Object value;

}
