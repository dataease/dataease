package io.dataease.service.sys;


import io.dataease.controller.sys.response.RoleUserItem;
import io.dataease.ext.ExtSysRoleMapper;
import io.dataease.plugins.common.base.domain.SysRole;
import io.dataease.plugins.common.request.KeywordRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SysRoleService {

    @Resource
    private ExtSysRoleMapper extSysRoleMapper;

    public List<SysRole> query(KeywordRequest request) {

        return extSysRoleMapper.query(request);
    }

    public List<RoleUserItem> allRoles() {
        return extSysRoleMapper.queryAll();
    }


}
