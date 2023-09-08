package io.dataease.plugins.xpack.role.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class RoleUserRequest implements Serializable {

    @ApiModelProperty("角色ID")
    private Long roleId;
    @ApiModelProperty("搜索关键值")
    private String keyWord;
    @ApiModelProperty("名称排序0:none,1asc,2desc")
    private Integer nameSort = 0;
    @ApiModelProperty(value = "查询区间0:全部, 1:已绑定, 2:未绑定", allowableValues="0,1,2")
    private Integer section = 0;
}
