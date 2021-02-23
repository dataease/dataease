package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class SysMenu implements Serializable {
    private Long menuId;

    private Long pid;

    private Integer subCount;

    private Integer type;

    private String title;

    private String name;

    private String component;

    private Integer menuSort;

    private String icon;

    private String path;

    private Boolean iFrame;

    private Boolean cache;

    private Boolean hidden;

    private String permission;

    private String createBy;

    private String updateBy;

    private Long createTime;

    private Long updateTime;

    private static final long serialVersionUID = 1L;
}