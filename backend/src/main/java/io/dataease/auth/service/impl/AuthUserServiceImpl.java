package io.dataease.auth.service.impl;

import io.dataease.auth.api.dto.CurrentRoleDto;
import io.dataease.auth.entity.SysUserEntity;
import io.dataease.base.mapper.ext.AuthMapper;
import io.dataease.auth.service.AuthUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthUserServiceImpl implements AuthUserService {

    @Resource
    private AuthMapper authMapper;


    @Override
    public SysUserEntity getUser(String username){
        return authMapper.findUser(username);
    }
    @Override
    public List<String> roles(Long userId){
        return authMapper.roleCodes(userId);
    }
    @Override
    public List<String> permissions(Long userId){
        List<String> permissions = authMapper.permissions(userId);
        return permissions.stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
    }

    @Override
    public List<CurrentRoleDto> roleInfos(Long userId) {
        return authMapper.roles(userId);
    }
}
