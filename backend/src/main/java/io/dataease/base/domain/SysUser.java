package io.dataease.base.domain;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SysUser implements Serializable {
    @ApiModelProperty(value = "用户ID" , allowEmptyValue = false, position = 0)
    private Long userId;

    @ApiModelProperty(value = "组织ID" , allowEmptyValue = false, position = 7)
    private Long deptId;

    @ApiModelProperty(value = "账号" , required = true)
    private String username;

    @ApiModelProperty(value = "姓名" , required = true, position = 2)
    private String nickName;

    @ApiModelProperty(value = "性别" ,allowableValues = "男,女", allowEmptyValue = true, position = 5)
    private String gender;

    @ApiModelProperty(value = "电话" , allowEmptyValue = true, position = 1)
    private String phone;

    @ApiModelProperty(value = "邮箱" , required = true, position = 3)
    private String email;

    @ApiModelProperty(value = "密码" , required = true, position = 4)
    private String password;

    @ApiModelProperty(hidden = true)
    private Boolean isAdmin;

    @ApiModelProperty(value = "状态" , allowableValues = "1,0", required = true, position = 6)
    private Long enabled;

    @ApiModelProperty(hidden = true)
    private String createBy;

    @ApiModelProperty(hidden = true)
    private String updateBy;

    @ApiModelProperty(hidden = true)
    private Long pwdResetTime;

    @ApiModelProperty(hidden = true)
    private Long createTime;

    @ApiModelProperty(hidden = true)
    private Long updateTime;

    @ApiModelProperty(hidden = true)
    private String language;

    private static final long serialVersionUID = 1L;
}