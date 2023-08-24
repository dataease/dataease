package io.dataease.plugins.common.base.domain;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DatasetTableUnion implements Serializable {
    @ApiModelProperty("ID")
    private String id;
    @ApiModelProperty("源表ID")
    private String sourceTableId;
    @ApiModelProperty("源表字段ID")
    private String sourceTableFieldId;
    @ApiModelProperty("源合并关系")
    private String sourceUnionRelation;
    @ApiModelProperty("目标字段ID")
    private String targetTableId;
    @ApiModelProperty("目标表字段ID")
    private String targetTableFieldId;
    @ApiModelProperty("目标合并关系")
    private String targetUnionRelation;
    @ApiModelProperty("创建者")
    private String createBy;
    @ApiModelProperty("创建时间")
    private Long createTime;

    private static final long serialVersionUID = 1L;
}