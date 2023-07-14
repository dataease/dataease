package io.dataease.api.permissions.dataset.dto;

import io.dataease.api.permissions.role.vo.RoleVO;
import io.dataease.api.permissions.user.vo.UserFormVO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DataSetRowPermissionsTreeDTO  {

    private Long id;

    /**
     * 是否启用
     */
    private Boolean enable;

    /**
     * 权限类型：dept/role/user
     */
    private String authTargetType;

    /**
     * 权限对象ID
     */
    private Long authTargetId;

    /**
     * 数据集ID
     */
    private Long datasetId;

    /**
     * 关系表达式
     */
    private String expressionTree;

    /**
     * 用户白名单
     */
    private String whiteListUser;

    /**
     * 角色白名单
     */
    private String whiteListRole;

    /**
     * 组织白名单
     */
    private String whiteListDept;

    private Long updateTime;
    private String datasetName;

    private String authTargetName;

    private DatasetRowPermissionsTreeObj tree;
    private List<UserFormVO> whiteListUsers;
    private List<RoleVO> whiteListRoles;
    private List<Long> authTargetIds;
}
