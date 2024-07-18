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

    @PostMapping("/save/oidc")
    String saveOidc(@RequestBody XpackOidcVO editor);

    @PostMapping("/save/cas")
    String saveCas(@RequestBody XpackCasVO editor);

    @PostMapping("/save/ldap")
    String saveLdap(@RequestBody XpackLdapVO editor);


    @GetMapping("/info/oidc")
    XpackOidcVO oidcInfo();

    @GetMapping("/info/cas")
    XpackCasVO casInfo();

    @GetMapping("/info/ldap")
    XpackLdapVO ldapInfo();


    @PostMapping("/validate/oidc")
    String validateOidc(@RequestBody XpackOidcVO editor);

    @PostMapping("/validate/cas")
    String validateCas(@RequestBody XpackCasVO editor);

    @PostMapping("/validate/ldap")
    String validateLdap(@RequestBody XpackLdapVO editor);

    @PostMapping("/validateId/{id}")
    String validate(@PathVariable("id") Long id);

    @Operation(summary = "查询状态")
    @GetMapping("/status")
    List<XpackAuthenticationStatusVO> status();
}
