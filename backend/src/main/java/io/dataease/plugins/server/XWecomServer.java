package io.dataease.plugins.server;


import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.display.dto.response.SysSettingDto;
import io.dataease.plugins.xpack.wecom.dto.response.WecomInfo;
import io.dataease.plugins.xpack.wecom.service.WecomXpackService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@ApiIgnore
@RequestMapping("/plugin/wecom")
@RestController
public class XWecomServer {

    @GetMapping("/info")
    public WecomInfo getWecomInfo() {
        WecomXpackService wecomXpackService = SpringContextUtil.getBean(WecomXpackService.class);
        return wecomXpackService.info();
    }

    @RequiresPermissions("sysparam:read")
    @PostMapping("/save")
    public void save(@RequestBody List<SysSettingDto> settings) {
        WecomXpackService wecomXpackService = SpringContextUtil.getBean(WecomXpackService.class);
        wecomXpackService.save(settings);
    }

    @PostMapping("/testConn")
    public void testConn(@RequestBody WecomInfo wecomInfo) {
        WecomXpackService wecomXpackService = SpringContextUtil.getBean(WecomXpackService.class);
        try {
            wecomXpackService.testConn(wecomInfo);
        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
