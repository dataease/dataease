package io.dataease.controller.staticResource;

import io.dataease.service.staticResource.StaticResourceService;
import io.swagger.annotations.ApiOperation;
import org.pentaho.ui.xul.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * Author: wangjiahao
 * Date: 2022/4/24
 * Description:
 */
@RestController
@RequestMapping("/static/resource")
public class StaticResourceController {

    @Resource
    StaticResourceService staticResourceService;

    @PostMapping("upload/{fileId}")
    @ApiOperation("Uploads static file")
    public void upload(@PathVariable("fileId") String fileId, @RequestPart("file") MultipartFile file) {
        staticResourceService.upload(fileId,file);
    }
}
