package io.dataease.controller.staticResource;

import io.dataease.service.staticResource.StaticResourceService;
import io.swagger.annotations.ApiOperation;
import org.pentaho.ui.xul.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
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

    @PostMapping("upload")
    @ApiOperation("Uploads static file")
    public void upload(@RequestPart("file") MultipartFile file) {
        staticResourceService.upload(file);
    }
}
