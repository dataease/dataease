package io.dataease.base.mapper.ext;

import io.dataease.controller.sys.request.UserGridRequest;
import io.dataease.controller.sys.response.SysUserGridResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtSysUserMapper {

    List<SysUserGridResponse> query(@Param("request")UserGridRequest request);
}
