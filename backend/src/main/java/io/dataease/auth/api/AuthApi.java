package io.dataease.auth.api;

import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.auth.api.dto.LoginDto;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;


@Api(tags = "权限：权限管理")
@RequestMapping("/api/auth")
public interface AuthApi {


    @PostMapping("/login")
    Object login(LoginDto loginDto) throws Exception;


    @PostMapping("/userInfo")
    CurrentUserDto userInfo();

    @GetMapping("/isLogin")
    Boolean isLogin();

    @PostMapping("/logout")
    String logout();

    @PostMapping("/validateName")
    Boolean validateName(Map<String, String> nameDto);


    @GetMapping("/test")
    String test();
}
