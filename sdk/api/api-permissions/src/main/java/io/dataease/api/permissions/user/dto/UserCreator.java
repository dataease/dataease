package io.dataease.api.permissions.user.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class UserCreator implements Serializable {

    @Serial
    private static final long serialVersionUID = 5231186463604221044L;

    @ApiModelProperty("姓名")
    private String name;
    @ApiModelProperty("账号")
    private String account;
    @ApiModelProperty("邮箱")
    private String email;
    @ApiModelProperty("手机号前缀")
    private String phonePrefix;
    @ApiModelProperty("手机号")
    private String phone;
    @ApiModelProperty("角色ID集合")
    private List<Long> roleIds;
    @ApiModelProperty("状态")
    private Boolean enable;
}
