package io.dataease.ext;

import io.dataease.controller.sys.request.UserGridRequest;
import io.dataease.controller.sys.response.SysUserGridResponse;

import java.util.List;

public interface ExtSysUserMapper {
    List<SysUserGridResponse> query(UserGridRequest request);

    List<String> ldapUserNames(Integer from);

    String queryAdminEmail();
}
