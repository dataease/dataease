package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class SysDict implements Serializable {
    private String id;

    private String dictName;

    private String dictCode;

    private String description;

    private Boolean delFlag;

    private String createBy;

    private Long createTime;

    private String updateBy;

    private Long updateTime;

    private static final long serialVersionUID = 1L;
}