package io.dataease.base.mapper.ext;

import io.dataease.controller.sys.request.RoleGridRequest;
import io.dataease.controller.sys.response.RoleNodeResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


public interface ExtSysRoleMapper {


    List<RoleNodeResponse> query(@Param("request")RoleGridRequest request);

    int deleteRoleMenu(@Param("roleId") Long roleId);

    int batchInsertRoleMenu(@Param("maps") List<Map<String, Long>> maps);
}
