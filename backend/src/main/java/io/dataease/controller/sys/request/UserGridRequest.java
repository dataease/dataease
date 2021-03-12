package io.dataease.controller.sys.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserGridRequest implements Serializable {
    @ApiModelProperty("快速检索")
    private String quick;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("组织")
    private String deptId;
    @ApiModelProperty("状态")
    private String enabled;
}
