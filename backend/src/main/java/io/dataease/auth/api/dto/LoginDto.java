package io.dataease.auth.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class LoginDto implements Serializable {

    @ApiModelProperty(value = "账号", required = true)
    private String username;

    @ApiModelProperty(value = "密码", required = true)
    private String password;

    /**
     * 0: 默认登录
     * 1：ldap登录
     * 2：单点登录
     */
    @ApiModelProperty(value = "登录方式{0:普通登录, 1:ldap登录}", required = true, allowableValues = "0, 1")
    private int loginType;
}
