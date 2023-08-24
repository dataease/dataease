package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class PanelSubject implements Serializable {
    private String id;

    private String name;

    private String type;

    private Integer createNum;

    private Long createTime;

    private String createBy;

    private Long updateTime;

    private String updateBy;

    private String details;

    private static final long serialVersionUID = 1L;
}