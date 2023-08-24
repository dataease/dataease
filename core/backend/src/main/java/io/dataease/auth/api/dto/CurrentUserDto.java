package io.dataease.auth.api.dto;

import io.dataease.auth.entity.SysUserEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class CurrentUserDto extends SysUserEntity implements Serializable {

    @ApiModelProperty("角色集合")
    private List<CurrentRoleDto> roles;

    @ApiModelProperty("权限集合")
    private List<String> permissions;

    public CurrentUserDto(String username, String nickName) {
        super.setUsername(username);
        super.setNickName(nickName);
    }
}
