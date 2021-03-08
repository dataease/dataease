package io.dataease.auth.service.impl;

import io.dataease.auth.api.dto.CurrentRoleDto;
import io.dataease.auth.entity.SysUserEntity;
import io.dataease.base.mapper.ext.AuthMapper;
import io.dataease.auth.service.AuthUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthUserServiceImpl implements AuthUserService {

    private final String USER_CACHE_NAME = "users_info";

    @Resource
    private AuthMapper authMapper;

    /**
     * 此处需被F2CRealm登录认证调用 也就是说每次请求都会被调用 所以最好加上缓存
     * @param userId
     * @return
     */
    @Cacheable(value = USER_CACHE_NAME,  key = "'user' + #userId" )
    @Override
    public SysUserEntity getUserById(Long userId){
        return authMapper.findUser(userId);
    }

    @Override
    public SysUserEntity getUserByName(String username) {
        return authMapper.findUserByName(username);
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
