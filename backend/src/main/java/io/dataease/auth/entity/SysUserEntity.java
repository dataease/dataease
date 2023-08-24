package io.dataease.auth.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data

public class SysUserEntity implements Serializable {

    @ApiModelProperty(hidden = true)
    private Long userId;

    @ApiModelProperty("账号")
    private String username;

    @ApiModelProperty("姓名")
    private String nickName;

    @ApiModelProperty("组织ID")
    private Long deptId;

    @ApiModelProperty("组织名称")
    private String deptName;

    @ApiModelProperty(hidden = true)
    private String password;

    @ApiModelProperty("状态")
    private Integer enabled;

    @ApiModelProperty("邮箱")
    private String email;

    @ApiModelProperty("电话")
    private String phone;

    @ApiModelProperty(hidden = true)
    private String language;

    @ApiModelProperty(hidden = true)
    private Boolean isAdmin;

    @ApiModelProperty(hidden = true)
    private Integer from;
}
