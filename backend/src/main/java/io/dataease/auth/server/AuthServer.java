package io.dataease.auth.server;

import io.dataease.auth.api.AuthApi;
import io.dataease.auth.api.dto.CurrentRoleDto;
import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.auth.api.dto.LoginDto;
import io.dataease.auth.entity.SysUserEntity;
import io.dataease.auth.service.AuthUserService;
import io.dataease.auth.util.JWTUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.ServletUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class AuthServer implements AuthApi {

    @Autowired
    private AuthUserService authUserService;


    @Override
    public void login(@RequestBody LoginDto loginDto) {
        String username = loginDto.getUsername();
        String password = loginDto.getPassword();
        SysUserEntity user = authUserService.getUser(username);
        String realPwd = user.getPassword();
        if (StringUtils.isEmpty(realPwd)){
            throw new RuntimeException("没有该用户！");
        }
        /*String pwd = RsaUtil.decryptByPrivateKey(RsaProperties.privateKey, password);
        String realPass = RsaUtil.decryptByPrivateKey(RsaProperties.privateKey, realPwd);
        if (!StrUtil.equals(pwd, realPass)){
            throw new RuntimeException("密码错误！");
        }*/
        if (!StringUtils.equals(realPwd, password)){
            throw new RuntimeException("密码错误！");
        }
        /*Map<String,Object> result = new HashMap<>();
        result.put("token", JWTUtils.sign(username, realPwd));*/
        String token = JWTUtils.sign(username, realPwd);
        ServletUtils.setToken(token);
    }

    @Override
    public CurrentUserDto userInfo() {
        String token = ServletUtils.getToken();
        String username = JWTUtils.getUsername(token);
        SysUserEntity user = authUserService.getUser(username);
        CurrentUserDto currentUserDto = BeanUtils.copyBean(new CurrentUserDto(), user);
        List<CurrentRoleDto> currentRoleDtos = authUserService.roleInfos(user.getUserId());
        currentUserDto.setRoles(currentRoleDtos);
        return currentUserDto;
    }

    @PostMapping("/logout")
    public String logout(){
        return "success";
    }

    @Override
    public Boolean isLogin() {
        return null;
    }

    @Override
    public String test() {
        return "apple";
    }
}
