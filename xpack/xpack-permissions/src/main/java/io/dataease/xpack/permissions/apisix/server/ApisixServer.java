package io.dataease.xpack.permissions.apisix.server;

import io.dataease.utils.ServletUtils;
import io.dataease.xpack.permissions.apisix.manage.ApisixManage;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/apisix")
public class ApisixServer {

    @Resource
    private ApisixManage apisixManage;

    @RequestMapping("/check")
    public boolean check() {
        HttpServletRequest request = ServletUtils.request();
        apisixManage.checkAuthenticationInfo(request);
        apisixManage.checkAuthorizationInfo(request);
        return true;
    }
}
