package io.dataease.ext;


import io.dataease.auth.entity.AuthItem;
import io.dataease.plugins.common.base.domain.SysAuth;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface ExtAuthMapper {

    List<Long> queryUserIdWithRoleIds(@Param("roleIds") List<Long> roleIds);
    List<String> queryUserNameWithRoleIds(@Param("roleIds") List<Long> roleIds);

    List<Long> queryUserIdWithDeptIds(@Param("deptIds") List<Long> deptIds);
    List<String> queryUserNameWithDeptIds(@Param("deptIds") List<Long> deptIds);

    List<SysAuth> queryByResource(@Param("resourceId") String resourceId);

    List<AuthItem> queryAuthItems(@Param("authTargetType") String authTargetType,
                                  @Param("authTarget") String authTarget,
                                  @Param("sourceType") String sourceType);

    String parentResource(@Param("resourceId") String resourceId, @Param("type") String type);
}
