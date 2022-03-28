package io.dataease.base.domain;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DeEngine implements Serializable {
    private String id;

    private String name;

    private String desc;

    private String type;

    private Long createTime;

    private Long updateTime;

    private String createBy;

    private String status;

    @ApiModelProperty(value = "详细信息", required = true)
    private String configuration;

    private static final long serialVersionUID = 1L;
}