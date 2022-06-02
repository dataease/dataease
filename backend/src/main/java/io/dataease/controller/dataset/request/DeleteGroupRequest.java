package io.dataease.controller.dataset.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class DeleteGroupRequest implements Serializable {

    @ApiModelProperty(value = "ID",required = true)
    private String id;
    @ApiModelProperty(value = "PID",required = true)
    private String pid;
}
