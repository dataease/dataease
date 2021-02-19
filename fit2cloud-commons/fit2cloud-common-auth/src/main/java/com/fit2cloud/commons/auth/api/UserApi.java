package com.fit2cloud.commons.auth.api;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.asymmetric.RSA;
import com.fit2cloud.commons.auth.bean.LoginDto;
import com.fit2cloud.commons.auth.bean.UserBean;
import com.fit2cloud.commons.auth.bean.UserInfo;
import com.fit2cloud.commons.auth.config.RsaProperties;
import com.fit2cloud.commons.auth.service.UserService;
import com.fit2cloud.commons.auth.util.JWTUtil;
import com.fit2cloud.commons.auth.util.RsaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class UserApi {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginDto loginDto) throws Exception{
        String username = loginDto.getUsername();
        String password = loginDto.getPassword();
        String realPwd = userService.getPassword(username);

        if (StrUtil.isEmpty(realPwd)){
            throw new RuntimeException("没有该用户！");
        }
        String pwd = RsaUtil.decryptByPrivateKey(RsaProperties.privateKey, password);
        String realpwd = RsaUtil.decryptByPrivateKey(RsaProperties.privateKey, realPwd);
        if (!StrUtil.equals(pwd, realpwd)){
            throw new RuntimeException("密码错误！");
        }
        Map<String,Object> result = new HashMap<>();
        result.put("token", JWTUtil.sign(username, realpwd));
        return result;
    }

    @GetMapping("/info")
    public UserInfo getUserInfo(){
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        String token = request.getHeader("Authorization");
        String username = JWTUtil.getUsername(token);
        UserBean user = userService.getUser(username);
        String[] split = user.getRole().split(",");
        List<String> strings = Arrays.asList(split);
        UserInfo info = UserInfo.builder().name(user.getUsername()).roles(strings).avatar("http://fit2cloud.com").introduction(user.getUsername()).build();
        return info;
    }

    @PostMapping("/logout")
    public String logout(){
        return "success";
    }

}
