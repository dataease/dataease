package io.dataease.base.mapper.ext;

import org.apache.ibatis.annotations.Param;

public interface ExtOrganizationMapper {

    int checkSourceRole(@Param("sourceId") String sourceId,@Param("userId") String userId,@Param("roleId") String roleId);
}
