package io.dataease.api.sync.task.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author fit2cloud
 */
@Setter
@Getter
public class TableField {
    private String fieldSource;
    private String fieldName;
    private String remarks;
    private String fieldType;
    private int fieldSize;
    private boolean fieldPk;
    private boolean fieldIndex;
    private int accuracy;
    private Object defaultValue;

}
