package io.dataease.controller.sys.request;

import io.dataease.controller.sys.base.BaseGridRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class KeyGridRequest extends BaseGridRequest implements Serializable {
    @ApiModelProperty("关键字")
    private String keyWord;
}
