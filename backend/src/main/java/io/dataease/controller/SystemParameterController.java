package io.dataease.controller;

import io.dataease.base.domain.SystemParameter;
import io.dataease.commons.constants.ParamConstants;
import io.dataease.commons.constants.RoleConstants;
import io.dataease.dto.BaseSystemConfigDTO;
import io.dataease.dto.SystemParameterDTO;
import io.dataease.notice.domain.MailInfo;
import io.dataease.service.system.SystemParameterService;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/system")
public class SystemParameterController {
    @Resource
    private SystemParameterService systemParameterService;

    @PostMapping("/edit/email")
    @RequiresRoles(value = {RoleConstants.ADMIN})
    public void editMail(@RequestBody List<SystemParameter> systemParameter) {
        systemParameterService.editMail(systemParameter);
    }

    @PostMapping("/testConnection")
    @RequiresRoles(value = {RoleConstants.ADMIN})
    public void testConnection(@RequestBody HashMap<String, String> hashMap) {
        systemParameterService.testConnection(hashMap);
    }

    @GetMapping("/version")
    public String getVersion() {
        return systemParameterService.getVersion();
    }

    @GetMapping("/mail/info")
    @RequiresRoles(value = {RoleConstants.ADMIN})
    public MailInfo mailInfo() {
        return systemParameterService.mailInfo(ParamConstants.Classify.MAIL.getValue());
    }


    @GetMapping("/base/info")
    @RequiresRoles(value = {RoleConstants.ADMIN})
    public List<SystemParameterDTO> getBaseInfo () {
        return systemParameterService.getSystemParameterInfo(ParamConstants.Classify.BASE.getValue());
    }

    @GetMapping("/ui/info")
    @RequiresRoles(value = {RoleConstants.ADMIN})
    public List<SystemParameterDTO> getDisplayInfo () {
        return systemParameterService.getSystemParameterInfo(ParamConstants.Classify.UI.getValue());
    }

    @PostMapping(value="/save/ui", consumes = {"multipart/form-data"})
    @RequiresRoles(value = {RoleConstants.ADMIN})
    public void saveUIInfo (@RequestPart("request") Map<String,List<SystemParameterDTO>> systemParameterMap,@RequestPart(value = "files") List<MultipartFile> bodyFiles) throws IOException {
        systemParameterService.saveUIInfo(systemParameterMap,bodyFiles);
    }


}
