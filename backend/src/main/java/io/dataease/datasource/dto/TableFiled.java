package io.dataease.datasource.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TableFiled {

    private String fieldName;
    private String remarks;
    private String fieldType;
    private int fieldSize;

}
