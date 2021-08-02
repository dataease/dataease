package io.dataease.auth.api;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.auth.api.dto.LoginDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;


@Api(tags = "权限：权限管理")
@ApiSupport(order = 10)
@RequestMapping("/api/auth")
public interface AuthApi {


    @ApiOperation("登录")
    @PostMapping("/login")
    Object login(LoginDto loginDto) throws Exception;


    @ApiOperation("获取用户信息")
    @PostMapping("/userInfo")
    CurrentUserDto userInfo();


    /*@GetMapping("/isLogin")
    Boolean isLogin();*/


    @ApiOperation("登出")
    @PostMapping("/logout")
    String logout();


    @ApiOperation("验证账号")
    @PostMapping("/validateName")
    Boolean validateName(Map<String, String> nameDto);

}
