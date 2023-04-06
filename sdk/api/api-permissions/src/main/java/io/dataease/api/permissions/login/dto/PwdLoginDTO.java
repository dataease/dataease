package io.dataease.api.permissions.login.dto;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class PwdLoginDTO {

    @ApiModelProperty(value = "账号/手机/邮箱/", required = true)
    @NotBlank(message = "login.validator.name")
    private String name;

    @ApiModelProperty(value = "密码", required = true)
    @NotBlank(message = "login.validator.pwd")
    private String pwd;
}
