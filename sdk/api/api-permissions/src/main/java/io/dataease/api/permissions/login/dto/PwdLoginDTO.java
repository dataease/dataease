package io.dataease.api.permissions.login.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PwdLoginDTO {

    @ApiModelProperty(value = "账号/手机/邮箱/", required = true)
    @NotNull
    private String name;

    @ApiModelProperty(value = "密码", required = true)
    @NotNull
    private String pwd;
}
