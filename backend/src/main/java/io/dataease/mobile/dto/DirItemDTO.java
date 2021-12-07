package io.dataease.mobile.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("目录数据实体")
public class DirItemDTO implements Serializable {

    @ApiModelProperty("ID")
    private String id;
    @ApiModelProperty("名称")
    private String text;
    @ApiModelProperty(value = "类型", allowableValues = "{@code folder, panel}")
    private String type;
    @ApiModelProperty("子集数")
    private Integer subs;
}
