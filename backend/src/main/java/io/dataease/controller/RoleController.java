package io.dataease.controller;

import io.dataease.base.domain.Role;
import io.dataease.commons.constants.RoleConstants;
import io.dataease.service.RoleService;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("role")
@RestController
public class RoleController {

    @Resource
    private RoleService roleService;

    @GetMapping("/list/{sign}")
    public List<Role> getRoleList(@PathVariable String sign) {
        return roleService.getRoleList(sign);
    }

    @GetMapping("/all")
    @RequiresRoles(RoleConstants.ADMIN)
    public List<Role> getAllRole() {
        return roleService.getAllRole();
    }

}
