package io.dataease.system.manage;

import io.dataease.api.permissions.auth.dto.BusiPerCheckDTO;
import io.dataease.license.config.XpackInteract;
import org.springframework.stereotype.Component;

@Component
public class CorePermissionManage {

    @XpackInteract(value = "corePermissionManage", replace = true)
    public boolean checkAuth(BusiPerCheckDTO dto) {
        return true;
    }
}
