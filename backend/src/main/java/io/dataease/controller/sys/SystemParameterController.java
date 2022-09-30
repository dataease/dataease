package io.dataease.controller.sys;


import io.dataease.plugins.common.base.domain.SystemParameter;
import io.dataease.commons.constants.ParamConstants;
import io.dataease.controller.sys.response.BasicInfo;
import io.dataease.controller.sys.response.MailInfo;
import io.dataease.dto.SystemParameterDTO;
import io.dataease.listener.DatasetCheckListener;
import io.dataease.listener.util.CacheUtils;
import io.dataease.plugins.common.util.GlobalFileUtil;
import io.dataease.plugins.xpack.cas.dto.CasSaveResult;
import io.dataease.service.FileService;
import io.dataease.service.system.EmailService;
import io.dataease.service.system.SystemParameterService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.io.IOException;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiIgnore
@RestController
@RequestMapping(value = "/system")
public class SystemParameterController {
    @Resource
    private SystemParameterService systemParameterService;

    @Resource
    private FileService fileService;

    @Resource
    private EmailService emailService;


    @RequiresPermissions("sysparam:read")
    @GetMapping("/mail/info")
    public MailInfo mailInfo() {
        return emailService.mailInfo();
    }

    @RequiresPermissions("sysparam:read")
    @GetMapping("/basic/info")
    public BasicInfo basicInfo() {
        return systemParameterService.basicInfo();
    }

    @GetMapping("/requestTimeOut")
    public Integer RequestTimeOut() {
        BasicInfo basicInfo = systemParameterService.basicInfo();
        return StringUtils.isNotBlank(basicInfo.getFrontTimeOut()) ? Integer.parseInt(basicInfo.getFrontTimeOut()) : 10;
    }

    @RequiresPermissions("sysparam:read")
    @PostMapping("/edit/email")
    public void editMail(@RequestBody List<SystemParameter> systemParameter) {
        emailService.editMail(systemParameter);
    }

    @RequiresPermissions("sysparam:read")
    @PostMapping("/edit/basic")
    public CasSaveResult editBasic(@RequestBody List<SystemParameter> systemParameter) {
        String value = systemParameter.stream().filter(parameter -> parameter.getParamKey().equals("basic.frontTimeOut")).findFirst().get().getParamValue();
        if (StringUtils.isNotBlank(value)) {
            int timeout = Integer.parseInt(value);
            if (timeout < 0 || timeout > 300) { //增加了合法性检验
                throw new NumberFormatException("Timeout Range Error!");
            }
        }
        return systemParameterService.editBasic(systemParameter);
    }

    @PostMapping("/testConnection")
    public void testConnection(@RequestBody HashMap<String, String> hashMap) {
        emailService.testConnection(hashMap);
    }

    @GetMapping("/version")
    public String getVersion() {
        return systemParameterService.getVersion();
    }


    @RequiresPermissions("sysparam:read")
    @GetMapping("/base/info")
    public List<SystemParameterDTO> getBaseInfo() {
        return systemParameterService.getSystemParameterInfo(ParamConstants.Classify.BASE.getValue());
    }

    @GetMapping("/ui/info")
    public List<SystemParameterDTO> getDisplayInfo() {
        return systemParameterService.getSystemParameterInfo(ParamConstants.Classify.UI.getValue());
    }

    @GetMapping(value = "/ui/image/{imageId}", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public ResponseEntity<byte[]> image(@PathVariable("imageId") String imageId) {
        byte[] bytes = fileService.loadFileAsBytes(imageId);
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @GetMapping("/filedown/{fileId}/{fileName}/{suffix}")
    public ResponseEntity<ByteArrayResource> down(@PathVariable("fileId") String fileId, @PathVariable("fileName") String fileName, @PathVariable("suffix") String suffix) throws Exception {
        fileName = URLDecoder.decode(fileName, "UTF-8");
        fileName += ("." + suffix);
        return GlobalFileUtil.down(fileId, fileName);
    }

    @GetMapping(value = "/showpicture/{fileId}", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public ResponseEntity<byte[]> showPicture(@PathVariable("fileId") String fileId) throws Exception {

        return GlobalFileUtil.showPicture(fileId);
    }

    @PostMapping(value = "/save/ui", consumes = {"multipart/form-data"})
    public void saveUIInfo(@RequestPart("request") Map<String, List<SystemParameterDTO>> systemParameterMap, @RequestPart(value = "files", required = false) List<MultipartFile> bodyFiles) throws IOException {
        systemParameterService.saveUIInfo(systemParameterMap, bodyFiles);
    }

    @PostMapping(value = "/checkCustomDs")
    public boolean checkCustomDs() {
        try {
            Object cache = CacheUtils.get(DatasetCheckListener.CACHE_NAME, DatasetCheckListener.CACHE_KEY);
            return cache != null && (boolean) cache;
        } catch (Exception e) {
            return false;
        }
    }

    @PostMapping(value = "/defaultLoginType")
    public Integer defaultLoginType() {
        return systemParameterService.defaultLoginType();
    }

}
