package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
public class DataFillForm implements Serializable {
    private String id;

    private String name;

    private String pid;

    private Integer level;

    private String nodeType;

    private String tableName;

    private String datasource;

    private Boolean createIndex;

    private String createBy;

    private Date createTime;

    private String updateBy;

    private Date updateTime;

    private Boolean commitNewUpdate;

    private static final long serialVersionUID = 1L;
}