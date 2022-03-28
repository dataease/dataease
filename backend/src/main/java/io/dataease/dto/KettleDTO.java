package io.dataease.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class KettleDTO {
    @ApiModelProperty(value = "Kettle 地址", required = true)
    private String carte;
    @ApiModelProperty(value = "Kettle 端口", required = true)
    private String port;
    @ApiModelProperty(value = "Kettle 用户名", required = true)
    private String user;
    @ApiModelProperty(value = "Kettle 密码", required = true)
    private String passwd;
}
