package io.dataease.plugins.common.request.permission;

import io.dataease.plugins.common.base.domain.DatasetRowPermissionsTreeWithBLOBs;
import io.dataease.plugins.common.base.domain.SysDept;
import io.dataease.plugins.common.base.domain.SysRole;
import io.dataease.plugins.common.base.domain.SysUser;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DataSetRowPermissionsTreeDTO extends DatasetRowPermissionsTreeWithBLOBs {
    @ApiModelProperty("数据集名称")
    private String datasetName;
    @ApiModelProperty("授权主体名称：组织名称/角色名称/用户名称")
    private String authTargetName;
    @ApiModelProperty("权限树（解析自expressionTree参数）")
    private DatasetRowPermissionsTreeObj tree;
    @ApiModelProperty("白名单-用户（解析自whiteListUser参数）")
    private List<SysUser> whiteListUsers;
    @ApiModelProperty("白名单-角色（解析自whiteListRole参数）")
    private List<SysRole> whiteListRoles;
    @ApiModelProperty("白名单-组织（解析自whiteListDept参数）")
    private List<SysDept> whiteListDepts;
    private List<Long> authTargetIds;
}
