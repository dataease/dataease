package io.dataease.plugins.common.base.domain;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SysUser implements Serializable {
    @ApiModelProperty("ID")
    private Long userId;

    @ApiModelProperty("组织ID")
    private Long deptId;

    @ApiModelProperty("账号")
    private String username;

    @ApiModelProperty("姓名")
    private String nickName;

    @ApiModelProperty("性别")
    private String gender;

    @ApiModelProperty("电话")
    private String phone;

    @ApiModelProperty("邮箱")
    private String email;

    @ApiModelProperty(hidden = true)
    private String password;

    @ApiModelProperty("是否管理员")
    private Boolean isAdmin;

    @ApiModelProperty("状态")
    private Long enabled;

    @ApiModelProperty("创建人")
    private String createBy;

    @ApiModelProperty("修改人")
    private String updateBy;

    @ApiModelProperty("密码重置时间")
    private Long pwdResetTime;

    @ApiModelProperty("创建时间")
    private Long createTime;

    @ApiModelProperty("修改时间")
    private Long updateTime;

    @ApiModelProperty("语言")
    private String language;

    @ApiModelProperty("用户来源")
    private Integer from;

    @ApiModelProperty(hidden = true)
    private String sub;

    @ApiModelProperty("手机号前缀")
    private String phonePrefix;

    private static final long serialVersionUID = 1L;
}