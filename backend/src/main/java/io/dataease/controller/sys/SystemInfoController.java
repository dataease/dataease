package io.dataease.controller.sys;

import io.dataease.dto.UserLoginInfoDTO;
import io.dataease.service.SystemInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.io.IOException;

@ApiIgnore
@RestController
@RequestMapping("systemInfo")
public class SystemInfoController {
    @Resource
    private SystemInfoService systemInfoService;

    @GetMapping("userLoginInfo")
    public UserLoginInfoDTO userLoginInfo() throws IOException {
        return systemInfoService.getUserLoginInfo(null);
    }

    @GetMapping("proxyUserLoginInfo/{userId}")
    public UserLoginInfoDTO proxyUserLoginInfo(@PathVariable String userId) throws IOException {
        return systemInfoService.getUserLoginInfo(userId);
    }
}
