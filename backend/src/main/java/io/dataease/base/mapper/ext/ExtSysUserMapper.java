package io.dataease.base.mapper.ext;

import io.dataease.base.mapper.ext.query.GridExample;
import io.dataease.controller.sys.response.SysUserGridResponse;

import java.util.List;

public interface ExtSysUserMapper {
    List<SysUserGridResponse> query(GridExample example);

    List<String> ldapUserNames(Integer from);
}
