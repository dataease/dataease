package io.dataease.plugins.xpack.dept.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class XpackDeptUserRequest implements Serializable {
    @ApiModelProperty("组织ID")
    private Long deptId;
    @ApiModelProperty("关键字")
    private String keyWord;
    @ApiModelProperty(value = "查询区间", allowableValues="0:全部, 1:已绑定, 2:未绑定")
    private Integer section = 0;
    @ApiModelProperty(value = "是否查询组织字段", allowableValues="0:否, 1:是")
    private Integer showDept = 0;
    @ApiModelProperty(value = "是否查询角色字段", allowableValues="0:否, 1:是")
    private Integer showRole = 0;
    @ApiModelProperty("名称排序0:none,1asc,2desc")
    private Integer nameSort = 0;

}
