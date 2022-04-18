package io.datains.auth.service;

import java.util.List;

import org.apache.shiro.authc.AuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import groovy.lang.Lazy;
import io.datains.auth.api.dto.CurrentRoleDto;
import io.datains.auth.api.dto.CurrentUserDto;
import io.datains.auth.entity.SysUserEntity;
import io.datains.commons.utils.BeanUtils;

@Service
public class ProxyAuthService {

    @Autowired
    @Lazy
    private AuthUserService authUserService;

    public CurrentUserDto queryCacheUserDto(Long userId) {

        SysUserEntity user = authUserService.getUserById(userId);
        if (user == null) {
            throw new AuthenticationException("User didn't existed!");
        }
        if (user.getEnabled() == 0) {
            throw new AuthenticationException("User is valid!");
        }
        // 使用缓存
        List<CurrentRoleDto> currentRoleDtos = authUserService.roleInfos(user.getUserId());
        // 使用缓存
        List<String> permissions = authUserService.permissions(user.getUserId());
        CurrentUserDto currentUserDto = BeanUtils.copyBean(new CurrentUserDto(), user);
        currentUserDto.setRoles(currentRoleDtos);
        currentUserDto.setPermissions(permissions);
        return currentUserDto;
    }

}
