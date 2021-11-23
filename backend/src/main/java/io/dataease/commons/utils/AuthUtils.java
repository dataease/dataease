package io.dataease.commons.utils;

import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.auth.service.ExtAuthService;
import io.dataease.commons.model.AuthURD;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Set;

@Component
public class AuthUtils {

    private static ExtAuthService extAuthService;

    @Autowired
    public void setExtAuthService(ExtAuthService extAuthService) {
        AuthUtils.extAuthService = extAuthService;
    }

    public static CurrentUserDto getUser() {
        CurrentUserDto userDto = (CurrentUserDto) SecurityUtils.getSubject().getPrincipal();
        return userDto;
    }

    //根据组织 角色 用户 获取下属用户ID
    public static Set<Long> userIdsByURD(AuthURD request) {
        Set<Long> userIds = extAuthService.userIdsByRD(request);
        if (!CollectionUtils.isEmpty(request.getUserIds())) {
            userIds.addAll(request.getUserIds());
        }
        return userIds;
    }

    // 获取资源对那些人/角色/组织 有权限
    public static AuthURD authURDR(String resourceId) {
        return extAuthService.resourceTarget(resourceId);
    }
}
