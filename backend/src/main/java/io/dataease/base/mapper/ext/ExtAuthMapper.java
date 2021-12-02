package io.dataease.base.mapper.ext;


import io.dataease.base.domain.SysAuth;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface ExtAuthMapper {

    List<Long> queryUserIdWithRoleIds(@Param("roleIds") List<Long> roleIds);

    List<Long> queryUserIdWithDeptIds(@Param("deptIds") List<Long> deptIds);

    List<SysAuth> queryByResource(@Param("resourceId") String resourceId);
}
