package io.dataease.api.visualization;

import io.dataease.api.visualization.request.StaticResourceRequest;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface StaticResourceApi {
    @PostMapping("upload/{fileId}")
    @ApiOperation("上传静态文件")
    public void upload(@PathVariable("fileId") String fileId, @RequestPart("file") MultipartFile file);

    @PostMapping("findResourceAsBase64")
    @ApiOperation("查找静态文件并转为Base64")
    public Map<String,String> findResourceAsBase64(@RequestBody StaticResourceRequest resourceRequest);

    @GetMapping("urlTest")
    public Map<String,String> urlTest();

}
