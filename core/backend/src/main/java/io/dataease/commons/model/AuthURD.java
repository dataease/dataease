package io.dataease.commons.model;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class AuthURD implements Serializable {

    @ApiModelProperty("用户ID集合")
    private List<Long> userIds;

    @ApiModelProperty("组织ID集合")
    private List<Long> deptIds;

    @ApiModelProperty("角色ID集合")
    private List<Long> roleIds;


}
