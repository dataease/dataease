package io.dataease.plugins.server;


import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.display.dto.response.SysSettingDto;
import io.dataease.plugins.xpack.display.service.DisPlayXpackService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RequestMapping("/api/display")
@RestController
public class DisplayServer {




    @GetMapping("/uiInfo")
    public List<SysSettingDto> uiInfo() {
        DisPlayXpackService disPlayXpackService = SpringContextUtil.getBean(DisPlayXpackService.class);
        return disPlayXpackService.systemSettings();
    }

    @PostMapping(value="/save", consumes = {"multipart/form-data"})
    public void saveUIInfo(@RequestPart("request") Map<String,List<SysSettingDto>> systemParameterMap, @RequestPart(value = "files", required = false) List<MultipartFile> bodyFiles) throws Exception {
        DisPlayXpackService disPlayXpackService = SpringContextUtil.getBean(DisPlayXpackService.class);
        disPlayXpackService.save(systemParameterMap, bodyFiles);
    }


}
