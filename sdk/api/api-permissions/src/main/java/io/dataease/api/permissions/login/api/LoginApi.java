package io.dataease.api.permissions.login.api;


import io.dataease.api.permissions.login.dto.PwdLoginDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 登录其实就是获得token的过程，最后把token返回到前端
 * 本地登录：账号密码获取用户获取token
 * oidc登录：回调请求头部X-Userinfo中获取用户 根据用户信息(没有就新建用户)再换取token
 */
public interface LoginApi {
    /**
     * 本地登录
     *
     * @param dto
     */
    @PostMapping("/login/localLogin")
    String localLogin(@Valid @RequestBody PwdLoginDTO dto);

    @PostMapping("/login/platformLogin/{origin}")
    String platformLogin(@PathVariable("origin") Integer origin);

    @GetMapping("/logout")
    void logout();

}
