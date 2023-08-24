package io.dataease.service;

import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.IPUtils;
import io.dataease.dto.UserLoginInfoDTO;
import io.dataease.plugins.common.base.domain.SysUser;
import io.dataease.plugins.common.base.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
public class SystemInfoService {
    @Resource
    private SysUserMapper sysUserMapper;

    public UserLoginInfoDTO getUserLoginInfo(Long userId) {
        if (userId != null) {
            SysUser userInfo = sysUserMapper.selectByPrimaryKey(userId);
            return new UserLoginInfoDTO(userInfo.getUsername(), userInfo.getNickName(), IPUtils.get());
        }
        CurrentUserDto userDto = AuthUtils.getUser();
        if (userDto != null) {
            return new UserLoginInfoDTO(userDto.getUsername(), userDto.getNickName(), IPUtils.get());
        } else {
            return new UserLoginInfoDTO(null, null, IPUtils.get());
        }
    }


}
