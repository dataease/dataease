package io.dataease.plugins.server;


import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.display.dto.response.SysSettingDto;
import io.dataease.plugins.xpack.display.service.DisplayXpackService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;
@ApiIgnore
@RequestMapping("/api/display")
@RestController
public class XDisplayServer {




    @GetMapping("/uiInfo")
    public List<SysSettingDto> uiInfo() {
        DisplayXpackService disPlayXpackService = SpringContextUtil.getBean(DisplayXpackService.class);
        return disPlayXpackService.systemSettings();
    }

    @RequiresPermissions("sysparam:read")
    @PostMapping(value="/save", consumes = {"multipart/form-data"})
    public void saveUIInfo(@RequestPart("request") Map<String,List<SysSettingDto>> systemParameterMap, @RequestPart(value = "files", required = false) List<MultipartFile> bodyFiles) throws Exception {
        DisplayXpackService disPlayXpackService = SpringContextUtil.getBean(DisplayXpackService.class);
        disPlayXpackService.save(systemParameterMap, bodyFiles);
    }


}
