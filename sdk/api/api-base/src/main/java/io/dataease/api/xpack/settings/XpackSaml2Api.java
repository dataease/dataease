package io.dataease.api.xpack.settings;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Tag(name = "Saml2认证")
@ApiSupport(order = 899)
public interface XpackSaml2Api {

    @PostMapping("/sso")
    String sso();

    @GetMapping(value = "/metadata", produces = MediaType.APPLICATION_XML_VALUE)
    String metadata();

    @GetMapping("/login")
    void login();
}
