package io.dataease.plugins.server;


import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.display.dto.response.SysSettingDto;
import io.dataease.plugins.xpack.oidc.service.OidcXpackService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

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

    @PostMapping(value="/authInfo")
    public Map<String, Object> authInfo() {
        OidcXpackService oidcXpackService = SpringContextUtil.getBean(OidcXpackService.class);
        Map<String, Object> result = new HashMap<String, Object>();
        List<SysSettingDto> oidcSettings = oidcXpackService.oidcSettings();
        
        Map<String, String> authParam = new HashMap<>();
        authParam.put("response_type", "code");
        authParam.put("state", "state");
        

        oidcSettings.forEach(param -> {
            if(StringUtils.isNotBlank(param.getParamKey())) {
                if (StringUtils.equals(param.getParamKey(), "oidc.authEndpoint")) {
                    result.put("authEndpoint", param.getParamValue());
                }
                if (StringUtils.equals(param.getParamKey(), "oidc.scope")) {
                    authParam.put("scope", param.getParamValue());
                }
                if (StringUtils.equals(param.getParamKey(), "oidc.clientId")) {
                    authParam.put("client_id", param.getParamValue());
                }
            }
                      
        });
        result.put("authParam", authParam);
        return result;
    }
}
