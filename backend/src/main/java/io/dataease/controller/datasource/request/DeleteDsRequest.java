package io.dataease.controller.datasource.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class DeleteDsRequest implements Serializable {

    @ApiModelProperty(value = "ID",required = true)
    private String id;

    @ApiModelProperty(value = "类型", required = true)
    private String type;
}
