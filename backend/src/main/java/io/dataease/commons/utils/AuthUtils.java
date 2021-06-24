package io.dataease.commons.utils;

import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.service.sys.SysUserService;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthUtils {

    private static SysUserService sysUserService;

    @Autowired
    public void setSysUserService(SysUserService sysUserService) {
        AuthUtils.sysUserService = sysUserService;
    }

    public static CurrentUserDto getUser(){
        CurrentUserDto userDto = (CurrentUserDto)SecurityUtils.getSubject().getPrincipal();
        return userDto;
    }
}
