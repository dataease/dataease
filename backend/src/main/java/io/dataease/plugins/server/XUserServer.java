package io.dataease.plugins.server;

import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.user.service.UserXpackService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@Api(tags = "xpack：用户导入")
@RequestMapping("/plugin/user")
@RestController
public class XUserServer {

    @ApiOperation("下载模版")
    @PostMapping("/template")
    public void template(HttpServletResponse response) {
        UserXpackService userXpackService = SpringContextUtil.getBean(UserXpackService.class);
        userXpackService.templateDown(response);
    }

    @ApiOperation("导入")
    @PostMapping("/upload")
    public void upload(@RequestPart(value = "file", required = true) MultipartFile file, HttpServletResponse response) throws Exception{
        UserXpackService userXpackService = SpringContextUtil.getBean(UserXpackService.class);
        userXpackService.upload(file, response);
    }
}
