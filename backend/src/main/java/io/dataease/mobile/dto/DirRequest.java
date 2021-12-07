package io.dataease.mobile.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("目录查询条件")
public class DirRequest implements Serializable {

    @ApiModelProperty("父ID")
    private String pid;

    @ApiModelProperty("名称")
    private String name;

}
