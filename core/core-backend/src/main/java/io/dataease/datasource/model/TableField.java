package io.dataease.datasource.model;

import lombok.Data;


@Data
public class TableField {
    private String fieldName;
    private String remarks;
    private String dbFieldName;         // API、excel 为插入数据库，重命名字段
    private String type;               //SQL type from java.sql.Types
    private int precision;
    private int scale;
    private boolean checked = false;
    private String fieldType;
    private int deType;
    private String jsonPath;


}
