package io.dataease.extensions.sync.model;

import lombok.Data;

/**
 *
 * @author jianneng
 * @date 2025/11/11 17:53
 **/
@Data
public class TableFieldDTO {
    private String fieldSource;
    private String fieldSourceType;
    private String fieldName;
    private String remarks;
    private String fieldType;
    private int fieldSize;
    private int fieldPrecision;
    private boolean fieldPk;
    private boolean fieldIndex;
    private Object defaultValue;
}
