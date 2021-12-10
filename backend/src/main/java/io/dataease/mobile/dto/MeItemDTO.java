package io.dataease.mobile.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("个人信息")
public class MeItemDTO implements Serializable {
    @ApiModelProperty("组织名称")
    private String deptName;
    @ApiModelProperty("创建时间")
    private Long createTime;
}
