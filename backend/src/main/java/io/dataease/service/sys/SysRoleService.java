package io.dataease.service.sys;


import io.dataease.ext.ExtSysRoleMapper;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.controller.sys.response.RoleUserItem;
import io.dataease.plugins.common.base.domain.SysRole;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SysRoleService {

    @Resource
    private ExtSysRoleMapper extSysRoleMapper;

    public List<SysRole> query(BaseGridRequest request) {
        List<SysRole> result = extSysRoleMapper.query(request.convertExample());

        return result;
    }

    public List<RoleUserItem> allRoles() {
        return extSysRoleMapper.queryAll();
    }


}
