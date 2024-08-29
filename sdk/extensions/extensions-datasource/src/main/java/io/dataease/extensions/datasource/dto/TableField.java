package io.dataease.extensions.datasource.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
public class TableField implements Serializable {
    private String name;
    private String originName;
    private String type;               //SQL type from java.sql.Types
    private int precision;
    private long size;
    private int scale;
    private boolean checked = false;
    private String fieldType;
    private Integer deType;
    private Integer deExtractType;
    private int extField;
    private String jsonPath;
    private boolean primary;
    List<Object> value;

    private int inCount;

}
