package io.dataease.service;

import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.IPUtils;
import io.dataease.dto.UserLoginInfoDTO;
import io.dataease.plugins.common.base.domain.SysUser;
import io.dataease.plugins.common.base.mapper.SysUserMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
public class SystemInfoService {
    @Resource
    private SysUserMapper sysUserMapper;

    public UserLoginInfoDTO getUserLoginInfo(String userId) {
        if (StringUtils.isNotEmpty(userId)) {
            SysUser userInfo = sysUserMapper.selectByPrimaryKey(Long.parseLong(userId));
            CurrentUserDto userDto = new CurrentUserDto();
            BeanUtils.copyBean(userDto, userInfo);
            return new UserLoginInfoDTO(userDto, IPUtils.get());
        }
        return new UserLoginInfoDTO(AuthUtils.getUser(), IPUtils.get());
    }

}
