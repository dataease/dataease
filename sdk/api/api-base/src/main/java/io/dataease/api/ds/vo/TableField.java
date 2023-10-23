package io.dataease.api.ds.vo;

import lombok.Data;

import java.util.List;


@Data
public class TableField {
    private String name;
    private String originName;
    private String type;               //SQL type from java.sql.Types
    private int precision;
    private long size;
    private int scale;
    private boolean checked = false;
    private String fieldType;
    private int deType;
    private int deExtractType;
    private int extField;
    private String jsonPath;
    List<Object> value;


}
