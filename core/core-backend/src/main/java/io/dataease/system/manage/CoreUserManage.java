package io.dataease.system.manage;

import io.dataease.license.config.XpackInteract;
import org.springframework.stereotype.Component;

@Component
public class CoreUserManage {


    @XpackInteract(value = "coreUserManage", replace = true)
    public String getUserName(Long uid) {
        return "管理员";
    }
}
