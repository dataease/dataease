package io.dataease.extensions.datasource.dto;

import lombok.Data;

import java.util.List;


@Data
public class TableField {
    private String name;
    private String originName;
    private String type;               //SQL type from java.sql.Types
    private Integer precision;
    private Long size;
    private Integer scale;
    private Boolean checked = false;
    private String fieldType;
    private Integer deType;
    private Integer deExtractType;
    private Integer extField;
    private String jsonPath;
    List<Object> value;


}
