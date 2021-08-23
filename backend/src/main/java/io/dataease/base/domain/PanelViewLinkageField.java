package io.dataease.base.domain;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PanelViewLinkageField implements Serializable {
    @ApiModelProperty("ID")
    private String id;
    @ApiModelProperty("联动ID")
    private String linkageId;
    @ApiModelProperty("源字段")
    private String sourceField;
    @ApiModelProperty("目标字段")
    private String targetField;
    @ApiModelProperty("更新时间")
    private Long updateTime;

    private static final long serialVersionUID = 1L;
}