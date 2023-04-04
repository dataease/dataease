package io.dataease.datasource.model;

import lombok.Data;


@Data
public class TableField {
    private String fieldName;
    private String remarks;
    private String type;
    private boolean checked = false;
    private String fieldType;
    private long size;
    private long deType;
    private int accuracy;
    private String jsonPath;



}
