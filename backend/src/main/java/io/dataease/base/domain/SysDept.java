package io.dataease.base.domain;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
public class SysDept implements Serializable {
    private Long deptId;

    private Long pid;

    private Integer subCount;

    private String name;

    private Integer deptSort;

    private Boolean enabled;

    private String createBy;

    private String updateBy;

    private Date createTime;

    private Date updateTime;

    private static final long serialVersionUID = 1L;
}