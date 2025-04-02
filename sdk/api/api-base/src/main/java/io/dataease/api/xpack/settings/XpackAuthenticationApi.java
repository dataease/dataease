package io.dataease.api.xpack.settings;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.xpack.settings.request.XpackAuthenticationEditor;
import io.dataease.api.xpack.settings.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "认证设置")
@ApiSupport(order = 899)
public interface XpackAuthenticationApi {


    @Operation(summary = "查询列表")
    @GetMapping("/grid")
    List<XpackAuthenticationVO> grid();

    @Operation(summary = "切换状态")
    @PostMapping("/switchEnable")
    void switchEnable(@RequestBody XpackAuthenticationEditor editor);

    @Operation(summary = "保存OIDC")
    @PostMapping("/save/oidc")
    String saveOidc(@RequestBody XpackOidcVO editor);

    @Operation(summary = "保存CAS")
    @PostMapping("/save/cas")
    String saveCas(@RequestBody XpackCasVO editor);

    @Operation(summary = "保存LDAP")
    @PostMapping("/save/ldap")
    String saveLdap(@RequestBody XpackLdapVO editor);

    @Operation(summary = "保存OAuth2")
    @PostMapping("/save/oauth2")
    String saveOauth2(@RequestBody XpackOauth2VO editor);

    @Operation(summary = "OIDC信息")
    @GetMapping("/info/oidc")
    XpackOidcVO oidcInfo();

    @Operation(summary = "CAS信息")
    @GetMapping("/info/cas")
    XpackCasVO casInfo();

    @Operation(summary = "LDAP信息")
    @GetMapping("/info/ldap")
    XpackLdapVO ldapInfo();

    @Operation(summary = "OAuth2信息")
    @GetMapping("/info/oauth2")
    XpackOauth2VO oauth2Info();

    @Operation(summary = "验证OIDC")
    @PostMapping("/validate/oidc")
    String validateOidc(@RequestBody XpackOidcVO editor);

    @Operation(summary = "验证CAS")
    @PostMapping("/validate/cas")
    String validateCas(@RequestBody XpackCasVO editor);

    @Operation(summary = "验证LDAP")
    @PostMapping("/validate/ldap")
    String validateLdap(@RequestBody XpackLdapVO editor);

    @Operation(summary = "验证OAuth2")
    @PostMapping("/validate/oauth2")
    String validateOauth2(@RequestBody XpackOauth2VO editor);

    @Operation(summary = "验证")
    @PostMapping("/validateId/{id}")
    String validate(@PathVariable("id") Long id);

    @Operation(summary = "查询状态")
    @GetMapping("/status")
    List<XpackAuthenticationStatusVO> status();

}
