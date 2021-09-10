package io.dataease.plugins.server;


import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.display.dto.response.SysSettingDto;
import io.dataease.plugins.xpack.oidc.service.OidcXpackService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/plugin/oidc")
@RestController
public class XOidcServer {


    @PostMapping("/info")
    public List<SysSettingDto> getOidcInfo() {
        OidcXpackService oidcXpackService = SpringContextUtil.getBean(OidcXpackService.class);
        return oidcXpackService.oidcSettings();
    }

    @PostMapping("/save")
    public void save(@RequestBody List<SysSettingDto> settings) {
        OidcXpackService oidcXpackService = SpringContextUtil.getBean(OidcXpackService.class);
        oidcXpackService.save(settings);
    }
}
