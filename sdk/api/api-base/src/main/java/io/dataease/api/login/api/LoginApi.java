package io.dataease.api.login.api;


import io.dataease.api.login.dto.PwdLoginDTO;
import io.dataease.auth.DeApiPath;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 登录其实就是获得token的过程，最后把token返回到前端
 * 本地登录：账号密码获取用户获取token
 * oidc登录：回调请求头部X-Userinfo中获取用户 根据用户信息(没有就新建用户)再换取token
 */
@DeApiPath("/login")
public interface LoginApi {
    /**
     * 本地登录
     * @param dto
     */
    @PostMapping("/localLogin")
    String localLogin(@Valid @RequestBody PwdLoginDTO dto);

    /**
     * oidc登录
     * @param code
     * @param state
     */
    @GetMapping("/oidcLogin")
    String oidcLogin(@RequestParam("code") String code, @RequestParam("state") String state);
}
