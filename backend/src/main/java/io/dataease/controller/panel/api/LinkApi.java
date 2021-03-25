package io.dataease.controller.panel.api;


import io.dataease.controller.request.panel.link.LinkRequest;
import io.dataease.controller.request.panel.link.PasswordRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

@Api(tags = "仪表板：链接管理")
@RequestMapping("/api/link")
public interface LinkApi {


    @ApiOperation("设置密码")
    @PostMapping("/replacePwd")
    void replacePwd(PasswordRequest request);

    @ApiOperation("生成")
    @PostMapping("/generate")
    void generateLink(LinkRequest request);

}
