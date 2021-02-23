package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class SysRole implements Serializable {
    private Long roleId;

    private String name;

    private String description;

    private String createBy;

    private String updateBy;

    private Long createTime;

    private Long updateTime;

    private static final long serialVersionUID = 1L;
}