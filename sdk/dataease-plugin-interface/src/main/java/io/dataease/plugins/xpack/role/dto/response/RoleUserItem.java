package io.dataease.plugins.xpack.role.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RoleUserItem {
    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("账号")
    private String username;

    @ApiModelProperty("姓名")
    private String nickName;

    @ApiModelProperty("邮箱")
    private String email;
}
