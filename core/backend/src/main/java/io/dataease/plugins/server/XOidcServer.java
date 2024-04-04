package io.dataease.plugins.server;


import io.dataease.commons.utils.LogUtil;
import io.dataease.plugins.common.exception.DataEaseException;
import io.dataease.plugins.common.util.SpringContextUtil;
import io.dataease.plugins.xpack.display.dto.response.SysSettingDto;
import io.dataease.plugins.xpack.oidc.service.OidcXpackService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiIgnore
@RequestMapping("/plugin/oidc")
@RestController
public class XOidcServer {


    @PostMapping("/info")
    public List<SysSettingDto> getOidcInfo() {
        OidcXpackService oidcXpackService = SpringContextUtil.getBean(OidcXpackService.class);
        return oidcXpackService.oidcSettings();
    }

    @RequiresPermissions("sysparam:read")
    @PostMapping("/save")
    public void save(@RequestBody List<SysSettingDto> settings) {
        OidcXpackService oidcXpackService = SpringContextUtil.getBean(OidcXpackService.class);
        oidcXpackService.save(settings);
    }

    @PostMapping(value = "/authInfo")
    public Map<String, Object> authInfo() {
        OidcXpackService oidcXpackService = SpringContextUtil.getBean(OidcXpackService.class);
        Map<String, Object> result = new HashMap<String, Object>();
        List<SysSettingDto> oidcSettings = oidcXpackService.oidcSettings();

        Map<String, String> authParam = new HashMap<>();
        authParam.put("response_type", "code");
        authParam.put("state", "state");


        oidcSettings.forEach(param -> {
            if (StringUtils.isNotBlank(param.getParamKey())) {
                if (StringUtils.equals(param.getParamKey(), "oidc.authEndpoint")) {
                    result.put("authEndpoint", param.getParamValue());
                }
                if (StringUtils.equals(param.getParamKey(), "oidc.scope")) {
                    authParam.put("scope", param.getParamValue());
                }
                if (StringUtils.equals(param.getParamKey(), "oidc.clientId")) {
                    authParam.put("client_id", param.getParamValue());
                }
                if (StringUtils.equals(param.getParamKey(), "oidc.pkce") && StringUtils.isNotBlank(param.getParamValue())) {
                    String pkceMethod = param.getParamValue();
                    authParam.put("code_challenge_method", pkceMethod);
                    if (StringUtils.equalsIgnoreCase("S256", pkceMethod)) {
                        String verifier = generateVerifier();
                        authParam.put("code_challenge", s256Challenge(verifier));
                        authParam.put("state", verifier);
                    } else if (StringUtils.equalsIgnoreCase("plain", pkceMethod)) {
                        String verifier = generateVerifier();
                        authParam.put("code_challenge", verifier);
                        authParam.put("state", verifier);
                    } else {
                        authParam.remove("code_challenge_method");
                    }
                }
            }

        });
        result.put("authParam", authParam);
        return result;
    }

    private String generateVerifier() {
        SecureRandom sr = new SecureRandom();
        byte[] code = new byte[32];
        sr.nextBytes(code);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(code);
    }

    private String s256Challenge(String verifier) {
        try {
            byte[] bytes = verifier.getBytes(StandardCharsets.US_ASCII);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(bytes, 0, bytes.length);
            byte[] digest = md.digest();
            return org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString(digest);
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
            DataEaseException.throwException(e.getMessage());
        }
        return null;
    }
}
