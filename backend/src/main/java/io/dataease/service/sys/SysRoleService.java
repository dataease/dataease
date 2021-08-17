package io.dataease.service.sys;


/*import io.dataease.base.domain.SysRole;
import io.dataease.base.domain.SysUsersRolesExample;
import io.dataease.base.mapper.SysRoleMapper;
import io.dataease.base.mapper.SysUsersRolesMapper;*/
import io.dataease.base.domain.SysRole;
import io.dataease.base.mapper.ext.ExtSysRoleMapper;
/*import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.controller.sys.request.RoleMenusRequest;*/
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.controller.sys.response.RoleUserItem;
import org.springframework.stereotype.Service;
/*import org.springframework.transaction.annotation.Transactional;*/

import javax.annotation.Resource;
/*import java.util.HashMap;*/
import java.util.List;
/*import java.util.Map;
import java.util.stream.Collectors;*/

@Service
public class SysRoleService {

    /*@Resource
    private SysRoleMapper mapper;*/

    @Resource
    private ExtSysRoleMapper extSysRoleMapper;

    /*@Resource
    private SysUsersRolesMapper sysUsersRolesMapper;*/



    /*public int add(SysRole role){
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

    @Transactional
    public int delete(Long roleId){
        SysUsersRolesExample example = new SysUsersRolesExample();
        example.createCriteria().andRoleIdEqualTo(roleId);
        sysUsersRolesMapper.deleteByExample(example);//删除用户角色关联关系
        extSysRoleMapper.deleteRoleMenu(roleId);//删除菜单角色关联关系
        return mapper.deleteByPrimaryKey(roleId);
    }

    */
    public List<SysRole> query(BaseGridRequest request){
        List<SysRole> result = extSysRoleMapper.query(request.convertExample());

        return result;
    }
    /*

    public List<Long> menuIds(Long roleId){
        return extSysRoleMapper.menuIds(roleId);
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
    }*/

    public List<RoleUserItem> allRoles(){
        return extSysRoleMapper.queryAll();
    }


}
