package io.dataease.plugins.common.base.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DatasetRowPermissionsTreeWithBLOBs extends DatasetRowPermissionsTree implements Serializable {
    @ApiModelProperty("权限树->JSON Object")
    private String expressionTree;
    @ApiModelProperty("白名单-用户ID->JSON Array")
    private String whiteListUser;
    @ApiModelProperty("白名单-角色ID->JSON Array")
    private String whiteListRole;
    @ApiModelProperty("白名单-组织ID->JSON Array")
    private String whiteListDept;

    private static final long serialVersionUID = 1L;
}
