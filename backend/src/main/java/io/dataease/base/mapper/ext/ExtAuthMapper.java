package io.dataease.base.mapper.ext;


import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface ExtAuthMapper {

    List<Long> queryUserIdWithRoleIds(@Param("roleIds") List<Long> roleIds);

    List<Long> queryUserIdWithDeptIds(@Param("deptIds") List<Long> deptIds);


    // Set<Long> queryUserIdWithRD(@Param("roleIds") List<Long> roleIds, @Param("deptIds") List<Long> deptIds);
}
