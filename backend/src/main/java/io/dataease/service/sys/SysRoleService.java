package io.dataease.service.sys;


import io.dataease.base.domain.SysRole;
import io.dataease.base.mapper.SysRoleMapper;
import io.dataease.base.mapper.ext.ExtSysRoleMapper;
import io.dataease.controller.sys.request.RoleGridRequest;
import io.dataease.controller.sys.request.RoleMenusRequest;
import io.dataease.controller.sys.response.RoleNodeResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysRoleService {

    @Resource
    private SysRoleMapper mapper;

    @Resource
    private ExtSysRoleMapper extSysRoleMapper;


    public int add(SysRole role){
        Long now = System.currentTimeMillis();
        role.setCreateTime(now);
        role.setUpdateTime(now);
        return mapper.insert(role);
    }


    public int update(SysRole role){
        Long now = System.currentTimeMillis();
        role.setUpdateTime(now);
        return mapper.updateByPrimaryKey(role);
    }

    public int delete(Long roleId){
        return mapper.deleteByPrimaryKey(roleId);
    }


    public List<RoleNodeResponse> query(RoleGridRequest request){
        List<RoleNodeResponse> result = extSysRoleMapper.query(request);

        return result;
    }


    @Transactional
    public int batchSaveRolesMenus(RoleMenusRequest request){
        extSysRoleMapper.deleteRoleMenu(request.getRoleId());
        List<Map<String, Long>> maps = request.getMenuIds().stream().map(menuId -> {
            Map<String, Long> map = new HashMap<>();
            map.put("roleId", request.getRoleId());
            map.put("menuId", menuId);
            return map;
        }).collect(Collectors.toList());
        return extSysRoleMapper.batchInsertRoleMenu(maps);
    }


}
