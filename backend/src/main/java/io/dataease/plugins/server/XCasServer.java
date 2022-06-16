package io.dataease.plugins.server;


import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.cas.dto.CasSaveResult;
import io.dataease.plugins.xpack.cas.service.CasXpackService;
import io.dataease.plugins.xpack.display.dto.response.SysSettingDto;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@ApiIgnore
@RequestMapping("/plugin/cas")
@RestController
public class XCasServer {


    @PostMapping("/info")
    public List<SysSettingDto> getOidcInfo() {
        CasXpackService casXpackService = SpringContextUtil.getBean(CasXpackService.class);
        return casXpackService.casSettings();
    }

    @RequiresPermissions("sysparam:read")
    @PostMapping("/save")
    public CasSaveResult save(@RequestBody List<SysSettingDto> settings) {
        CasXpackService casXpackService = SpringContextUtil.getBean(CasXpackService.class);
        return casXpackService.save(settings);
    }
}
