package io.dataease.ext;

import io.dataease.controller.sys.response.RoleUserItem;
import io.dataease.plugins.common.base.domain.SysRole;
import io.dataease.plugins.common.request.KeywordRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


public interface ExtSysRoleMapper {


    List<SysRole> query(KeywordRequest request);

    int deleteRoleMenu(@Param("roleId") Long roleId);

    int batchInsertRoleMenu(@Param("maps") List<Map<String, Long>> maps);

    List<RoleUserItem> queryAll();

    List<Long> menuIds(@Param("roleId") Long roleId);
}
