package io.dataease.controller.datasource.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UpdataDsRequest {
    @ApiModelProperty(value = "ID",required = true)
    private String id;
    @ApiModelProperty(value = "名称",required = true)
    private String name;
    @ApiModelProperty("描述")
    private String desc;
    @ApiModelProperty(value = "类型", required = true)
    private String type;
    @ApiModelProperty(value = "配置详情", required = true)
    private String configuration;
    private boolean configurationEncryption = false;
}
