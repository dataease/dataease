package io.dataease.commons.utils;

import io.dataease.auth.entity.TokenInfo;
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
        TokenInfo tokenInfo = JWTUtils.tokenInfoByToken(token);
        SysUser sysUser = new SysUser();
        sysUser.setUserId(tokenInfo.getUserId());
        SysUser user = sysUserService.findOne(sysUser);
        return user;
    }
}
