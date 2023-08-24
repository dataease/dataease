package io.dataease.controller.sys.request;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("更新密码参数")
public class SysUserPwdRequest implements Serializable {

    @ApiModelProperty(value = "用户ID", required = true)
    private Long userId;
    @ApiModelProperty(value = "旧密码(密文)", required = true)
    private String password;
    @ApiModelProperty(hidden = true)
    private String repeatPassword;
    @ApiModelProperty(value = "新密码(明文)", required = true)
    private String newPassword;


}
