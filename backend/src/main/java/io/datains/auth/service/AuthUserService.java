package io.datains.auth.service;

import io.datains.auth.api.dto.CurrentRoleDto;
import io.datains.auth.entity.SysUserEntity;

import java.util.List;

public interface AuthUserService {


    SysUserEntity getUserById(Long userId);

    SysUserEntity getUserByName(String username);

    SysUserEntity getLdapUserByName(String username);

    SysUserEntity getUserBySub(String sub);

    List<String> roles(Long userId);

    List<String> permissions(Long userId);

    List<CurrentRoleDto> roleInfos(Long userId);

    void clearCache(Long userId);

    boolean supportLdap();

    Boolean supportOidc();

    Boolean pluginLoaded();


}
