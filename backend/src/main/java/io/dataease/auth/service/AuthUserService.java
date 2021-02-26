package io.dataease.auth.service;

import io.dataease.auth.api.dto.CurrentRoleDto;
import io.dataease.auth.entity.SysUserEntity;

import java.util.List;

public interface AuthUserService {



    SysUserEntity getUser(String username);

    List<String> roles(Long userId);

    List<String> permissions(Long userId);

    List<CurrentRoleDto> roleInfos(Long userId);



}
