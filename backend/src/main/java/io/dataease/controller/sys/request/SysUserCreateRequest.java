package io.dataease.controller.sys.request;

import io.dataease.plugins.common.base.domain.SysUser;
import io.dataease.plugins.common.base.domain.SysUserAssist;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("用户信息")
public class SysUserCreateRequest extends SysUser {

    @ApiModelProperty(value = "角色ID集合", required = true, position = 7)
    private List<Long> roleIds;

    @ApiModelProperty(value = "辅助信息", required = false, position = 8)
    private SysUserAssist sysUserAssist;

}
