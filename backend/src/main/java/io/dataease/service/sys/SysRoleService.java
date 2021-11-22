package io.dataease.service.sys;


import io.dataease.base.domain.SysRole;
import io.dataease.base.mapper.ext.ExtSysRoleMapper;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.controller.sys.response.RoleUserItem;
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
