package io.dataease.api.permissions.login.api;


import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.permissions.login.dto.MfaLoginDTO;
import io.dataease.api.permissions.login.dto.PwdLoginDTO;
import io.dataease.api.permissions.login.vo.MfaQrVO;
import io.dataease.api.permissions.user.dto.ModifyPwdRequest;
import io.dataease.auth.vo.TokenVO;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "登录")
@ApiSupport(order = 889, author = "fit2cloud-someone")
public interface LoginApi {
    /**
     * 本地登录
     *
     * @param dto
     */
    @Operation(summary = "本地登录")
    @ApiOperationSupport(order = 1)
    @PostMapping("/login/localLogin")
    TokenVO localLogin(@Valid @RequestBody PwdLoginDTO dto);

    @Operation(summary = "token续命", hidden = true)
    @ApiOperationSupport(order = 2)
    @GetMapping("/login/refresh")
    TokenVO refresh();

    @Operation(summary = "第三方登录", hidden = true)
    @ApiOperationSupport(order = 3)
    @PostMapping("/login/platformLogin/{origin}")
    TokenVO platformLogin(@PathVariable("origin") Integer origin);

    @Operation(summary = "登出")
    @ApiOperationSupport(order = 4)
    @GetMapping("/logout")
    void logout();

    @Operation(summary = "MFA二维码信息")
    @ApiOperationSupport(order = 5)
    @PostMapping("/mfa/qr/{id}")
    MfaQrVO mfaQr(@PathVariable("id") Long id);

    @Operation(summary = "MFA登录")
    @ApiOperationSupport(order = 6)
    @PostMapping("/mfa/login")
    TokenVO mfaLogin(@RequestBody MfaLoginDTO dto);

    @Operation(summary = "修改无效密码", hidden = true)
    @PostMapping("/login/modifyInvalidPwd")
    void modifyInvalidPwd(@RequestBody ModifyPwdRequest request);
}
