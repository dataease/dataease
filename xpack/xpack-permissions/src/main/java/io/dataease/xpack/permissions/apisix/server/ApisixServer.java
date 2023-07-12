package io.dataease.xpack.permissions.apisix.server;

import io.dataease.utils.LogUtil;
import io.dataease.utils.ServletUtils;
import io.dataease.xpack.permissions.apisix.manage.ApisixManage;
import io.dataease.xpack.permissions.apisix.manage.ApisixTokenManage;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/apisix")
public class ApisixServer {

    @Resource
    private ApisixManage apisixManage;

    @Resource
    private ApisixTokenManage apisixTokenManage;

    @RequestMapping("/check")
    public ResponseEntity<String> check() {
        if (apisixTokenManage.needAuth()) {
            HttpServletRequest request = ServletUtils.request();
            try {
                apisixManage.checkAuthenticationInfo(request);
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
                return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
            try {
                apisixManage.checkAuthorizationInfo(request);
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
                return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
            }
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
