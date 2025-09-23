package io.dataease.api.xpack.settings;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Tag(name = "Saml2认证")
@ApiSupport(order = 899)
public interface XpackSaml2Api {

    @GetMapping("/login")
    void saml2Login();

    @PostMapping("/sso")
    void saml2Callback() throws Exception;
}
