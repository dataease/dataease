package io.dataease.ext;

import io.dataease.ext.query.GridExample;
import io.dataease.controller.sys.response.RoleUserItem;
import io.dataease.plugins.common.base.domain.SysRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


public interface ExtSysRoleMapper {


    List<SysRole> query(GridExample example);

    int deleteRoleMenu(@Param("roleId") Long roleId);

    int batchInsertRoleMenu(@Param("maps") List<Map<String, Long>> maps);

    List<RoleUserItem> queryAll();

    List<Long> menuIds(@Param("roleId") Long roleId);
}
