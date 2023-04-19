package io.dataease.api.ds.vo;

import lombok.Data;


@Data
public class TableField {
    private String fieldName;
    private String remarks;
    private String type;               //SQL type from java.sql.Types
    private int precision;
    private int scale;
    private boolean checked = false;
    private String fieldType;
    private int deType;
    private String jsonPath;


}
