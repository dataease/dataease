package io.dataease.controller.sys.request;

import io.dataease.base.domain.SysUser;
import lombok.Data;

import java.util.List;

@Data
public class SysUserCreateRequest extends SysUser {

    private List<Long> roleIds;

}
