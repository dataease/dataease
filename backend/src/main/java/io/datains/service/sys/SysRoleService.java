package io.datains.service.sys;


import io.datains.base.domain.SysRole;
import io.datains.base.mapper.ext.ExtSysRoleMapper;
import io.datains.controller.sys.base.BaseGridRequest;
import io.datains.controller.sys.response.RoleUserItem;
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
