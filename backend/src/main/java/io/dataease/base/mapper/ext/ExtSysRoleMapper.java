package io.dataease.base.mapper.ext;

import io.dataease.controller.sys.request.RoleGridRequest;
import io.dataease.controller.sys.response.RoleNodeResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface ExtSysRoleMapper {


    List<RoleNodeResponse> query(@Param("request")RoleGridRequest request);
}
