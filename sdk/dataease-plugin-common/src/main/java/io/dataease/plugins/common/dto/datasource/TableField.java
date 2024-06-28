package io.dataease.plugins.common.dto.datasource;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TableField {
    private String fieldName;
    private String remarks;
    private String fieldType;
    private int fieldSize;
    private int accuracy;
    private boolean notNull;
    private boolean primaryKey;

    //java.sql.Types
    private Integer type;

    private int inCount;

}
