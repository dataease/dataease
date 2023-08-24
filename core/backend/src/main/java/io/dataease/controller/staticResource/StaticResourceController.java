package io.dataease.controller.staticResource;

import io.dataease.controller.request.resource.StaticResourceRequest;
import io.dataease.service.staticResource.StaticResourceService;
import io.swagger.annotations.ApiOperation;
import org.pentaho.ui.xul.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Author: wangjiahao
 * Date: 2022/4/24
 * Description:
 */
@RestController
@RequestMapping("/staticResource")
public class StaticResourceController {

    @Resource
    StaticResourceService staticResourceService;

    @PostMapping("upload/{fileId}")
    @ApiOperation("上传静态文件")
    public void upload(@PathVariable("fileId") String fileId, @RequestPart("file") MultipartFile file) {
        staticResourceService.upload(fileId,file);
    }

    @PostMapping("findResourceAsBase64")
    @ApiOperation("查找静态文件并转为Base64")
    public Map<String,String> findResourceAsBase64(@RequestBody StaticResourceRequest resourceRequest){
        return staticResourceService.findResourceAsBase64(resourceRequest);
    }
}
