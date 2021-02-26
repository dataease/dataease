package io.dataease.commons.utils;

import io.dataease.auth.util.JWTUtils;
import io.dataease.base.domain.SysUser;
import io.dataease.service.sys.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthUtils {

    private static SysUserService sysUserService;

    @Autowired
    public void setSysUserService(SysUserService sysUserService) {
        AuthUtils.sysUserService = sysUserService;
    }

    public static SysUser getUser(){
        String token = ServletUtils.getToken();
        String username = JWTUtils.getUsername(token);
        SysUser sysUser = new SysUser();
        sysUser.setUsername(username);
        SysUser user = sysUserService.findOne(sysUser);
        return user;
    }
}
