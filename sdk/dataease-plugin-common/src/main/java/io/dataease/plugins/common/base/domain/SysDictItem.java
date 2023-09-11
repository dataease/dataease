package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class SysDictItem implements Serializable {
    private String id;

    private String dictId;

    private String itemText;

    private String itemValue;

    private String description;

    private Integer sortOrder;

    private String status;

    private String createBy;

    private Long createTime;

    private String updateBy;

    private Long updateTime;

    private static final long serialVersionUID = 1L;
}