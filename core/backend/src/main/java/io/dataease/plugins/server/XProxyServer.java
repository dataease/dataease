package io.dataease.plugins.server;

import io.dataease.plugins.common.util.SpringContextUtil;
import io.dataease.plugins.xpack.display.dto.response.SysSettingDto;
import io.dataease.plugins.xpack.proxy.dto.ProxyInfo;
import io.dataease.plugins.xpack.proxy.service.ProxyXpackService;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@ApiIgnore
@RequestMapping("/plugin/proxy")
@RestController
public class XProxyServer {

    @GetMapping("/query")
    public ProxyInfo query() {
        ProxyXpackService xpackService = SpringContextUtil.getBean(ProxyXpackService.class);
        return xpackService.info();
    }

    @PostMapping("/save")
    public void save(@RequestBody List<SysSettingDto> list) {
        ProxyXpackService xpackService = SpringContextUtil.getBean(ProxyXpackService.class);
        xpackService.save(list);
    }
}
