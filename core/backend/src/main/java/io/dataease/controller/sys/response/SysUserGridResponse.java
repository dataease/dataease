package io.dataease.controller.sys.response;


import io.dataease.plugins.common.base.domain.SysUser;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;
@Data
public class SysUserGridResponse extends SysUser {

    @ApiModelProperty("ID")
    private Long id;
    @ApiModelProperty("角色集合")
    private List<SysUserRole> roles;
    @ApiModelProperty("组织信息")
    private SysUserDept dept;
    @ApiModelProperty("角色ID集合")
    private List<Long> roleIds;
    @ApiModelProperty("锁定")
    private Boolean locked;

}
