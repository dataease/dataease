package io.dataease.auth.server;

import io.dataease.auth.api.AuthApi;
import io.dataease.auth.api.dto.CurrentRoleDto;
import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.auth.api.dto.LoginDto;
import io.dataease.auth.config.RsaProperties;
import io.dataease.auth.entity.SysUserEntity;
import io.dataease.auth.entity.TokenInfo;
import io.dataease.auth.service.AuthUserService;
import io.dataease.auth.util.JWTUtils;
import io.dataease.auth.util.RsaUtil;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.CodingUtil;
import io.dataease.commons.utils.LogUtil;
import io.dataease.commons.utils.ServletUtils;

import io.dataease.exception.DataEaseException;
import io.dataease.i18n.Translator;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class AuthServer implements AuthApi {

    @Autowired
    private AuthUserService authUserService;


    @Override
    public Object login(@RequestBody LoginDto loginDto) throws Exception {
        String username = loginDto.getUsername();
        String password = loginDto.getPassword();
        SysUserEntity user = authUserService.getUserByName(username);

        if (ObjectUtils.isEmpty(user)) {
            DataEaseException.throwException(Translator.get("i18n_id_or_pwd_error"));
        }
        if (user.getEnabled() == 0) {
            DataEaseException.throwException(Translator.get("i18n_id_or_pwd_error"));
        }
        String realPwd = user.getPassword();
        //私钥解密
        String pwd = RsaUtil.decryptByPrivateKey(RsaProperties.privateKey, password);
        //md5加密
        pwd = CodingUtil.md5(pwd);

        if (!StringUtils.equals(pwd, realPwd)) {
            DataEaseException.throwException(Translator.get("i18n_id_or_pwd_error"));
        }
        Map<String, Object> result = new HashMap<>();
        TokenInfo tokenInfo = TokenInfo.builder().userId(user.getUserId()).username(username).build();
        String token = JWTUtils.sign(tokenInfo, realPwd);
        // 记录token操作时间
        result.put("token", token);
        ServletUtils.setToken(token);
        return result;
    }

    @Override
    public CurrentUserDto userInfo() {
        CurrentUserDto userDto = (CurrentUserDto) SecurityUtils.getSubject().getPrincipal();
        if (ObjectUtils.isEmpty(userDto)) {
            String token = ServletUtils.getToken();
            Long userId = JWTUtils.tokenInfoByToken(token).getUserId();
            SysUserEntity user = authUserService.getUserById(userId);
            CurrentUserDto currentUserDto = BeanUtils.copyBean(new CurrentUserDto(), user);
            List<CurrentRoleDto> currentRoleDtos = authUserService.roleInfos(user.getUserId());
            List<String> permissions = authUserService.permissions(user.getUserId());
            currentUserDto.setRoles(currentRoleDtos);
            currentUserDto.setPermissions(permissions);
            return currentUserDto;
        }
        return userDto;
    }

    @Override
    public String logout() {
        String token = ServletUtils.getToken();
        if (StringUtils.isEmpty(token) || StringUtils.equals("null", token) || StringUtils.equals("undefined", token)) {
            return "success";
        }
        try{
            Long userId = JWTUtils.tokenInfoByToken(token).getUserId();
            authUserService.clearCache(userId);
        }catch (Exception e) {
            LogUtil.error(e);
            return "fail";
        }

        return "success";
    }

    @Override
    public Boolean validateName(@RequestBody Map<String, String> nameDto) {
        String userName = nameDto.get("userName");
        if (StringUtils.isEmpty(userName)) return false;
        SysUserEntity userEntity = authUserService.getUserByName(userName);
        if (ObjectUtils.isEmpty(userEntity)) return false;
        return true;
    }

    /*@Override
    public Boolean isLogin() {
        return null;
    }*/


}
