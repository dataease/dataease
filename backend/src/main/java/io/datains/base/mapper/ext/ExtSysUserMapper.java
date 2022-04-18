package io.datains.base.mapper.ext;

import io.datains.base.mapper.ext.query.GridExample;
import io.datains.controller.sys.response.SysUserGridResponse;

import java.util.List;

public interface ExtSysUserMapper {
    List<SysUserGridResponse> query(GridExample example);

    List<String> ldapUserNames(Integer from);
}
