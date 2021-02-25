package io.dataease.controller.sys.response;


import io.dataease.base.domain.SysUser;
import lombok.Data;
import java.util.List;
@Data
public class SysUserGridResponse extends SysUser {

    private Long id;

    private List<SysUserRole> roles;

    private SysUserDept dept;

    private List<Long> roleIds;

}
