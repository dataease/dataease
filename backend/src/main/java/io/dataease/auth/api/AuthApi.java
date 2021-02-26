package io.dataease.auth.api;

import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.auth.api.dto.LoginDto;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Api(tags = "权限：权限管理")
@RequestMapping("/api/auth")
public interface AuthApi {


    @PostMapping("/login")
    void login(LoginDto loginDto);


    @PostMapping("/userInfo")
    CurrentUserDto userInfo();

    @GetMapping("/isLogin")
    Boolean isLogin();


    @GetMapping("/test")
    String test();
}
