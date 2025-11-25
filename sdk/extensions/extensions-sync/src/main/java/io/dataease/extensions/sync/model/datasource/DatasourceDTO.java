package io.dataease.extensions.sync.model.datasource;

import lombok.Data;

@Data
public class DatasourceDTO {
    private String id;
    private String name;
    private String desc;
    private String type;
    private String typeName;
    private String configuration;
    private Long createTime;
    private Long updateTime;
    private Long createBy;
    private String createByName;
    private String status;
    private String statusRemark;
    private Integer datasourceRole;
}
