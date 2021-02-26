package io.dataease.auth.service.impl;

import io.dataease.auth.api.dto.CurrentRoleDto;
import io.dataease.auth.entity.SysUserEntity;
import io.dataease.base.mapper.ext.AuthMapper;
import io.dataease.auth.service.AuthUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

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
        return authMapper.permissions(userId);
    }

    @Override
    public List<CurrentRoleDto> roleInfos(Long userId) {
        return authMapper.roles(userId);
    }
}
