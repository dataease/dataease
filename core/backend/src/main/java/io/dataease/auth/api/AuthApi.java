package io.dataease.auth.api;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.auth.api.dto.LoginDto;
import io.dataease.auth.api.dto.SeizeLoginDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

@Api(tags = "登录：登录管理")
@ApiSupport(order = 10)
@RequestMapping("/api/auth")
public interface AuthApi {

    @ApiOperation("登录")
    @PostMapping("/login")
    Object login(LoginDto loginDto) throws Exception;

    @ApiOperation("移动端登录")
    @PostMapping("/mobileLogin")
    Object mobileLogin(LoginDto loginDto) throws Exception;

    @PostMapping("/seizeLogin")
    Object seizeLogin(SeizeLoginDto loginDto) throws Exception;

    @ApiOperation("获取用户信息")
    @PostMapping("/userInfo")
    CurrentUserDto userInfo();

    @ApiOperation("是否使用初始密码")
    @PostMapping("/useInitPwd")
    Boolean useInitPwd();

    @ApiOperation("不再提示修改密码")
    @PostMapping("/removeNoti")
    void removeNoti();

    @ApiOperation("用户初始密码")
    @PostMapping("/defaultPwd")
    String defaultPwd();

    @ApiOperation("登出")
    @PostMapping("/logout")
    String logout();

    @ApiIgnore
    @PostMapping("/deLogout")
    String deLogout();

    @ApiOperation("验证账号")
    @PostMapping("/validateName")
    Boolean validateName(Map<String, String> nameDto);

    @ApiOperation("是否开启ldap")
    @PostMapping("/isOpenLdap")
    boolean isOpenLdap();

    @ApiOperation("是否开启oidc")
    @PostMapping("/isOpenOidc")
    boolean isOpenOidc();

    @ApiOperation("是否开启cas")
    @PostMapping("/isOpenCas")
    boolean isOpenCas();


    @ApiOperation("是否开启企业微信")
    @PostMapping("/isOpenWecom")
    boolean isOpenWecom();

    @ApiOperation("是否开启钉钉")
    @PostMapping("/isOpenDingtalk")
    boolean isOpenDingtalk();

    @ApiOperation("是否开启飞书")
    @PostMapping("/isOpenLark")
    boolean isOpenLark();

    @ApiOperation("是否开启国际飞书")
    @PostMapping("/isOpenLarksuite")
    boolean isOpenLarksuite();

    @ApiIgnore
    @PostMapping("/isPluginLoaded")
    boolean isPluginLoaded();

    @ApiIgnore
    @GetMapping("/getPublicKey")
    String getPublicKey();

}
