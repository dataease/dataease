package com.fit2cloud.commons.base.api;

import com.fit2cloud.commons.base.result.ResultEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PermissionErrorApi extends ResultEntity {
    @GetMapping("/permissionMiss")
    public ResultEntity error(){
        return permission();
    }
}
