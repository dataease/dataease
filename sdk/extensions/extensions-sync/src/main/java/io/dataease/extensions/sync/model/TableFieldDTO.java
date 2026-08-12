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
    /**
     * 源字段经过 DataEase 归一化后的标准类型
     */
    private String fieldSourceStandardType;
    private String fieldName;
    private String remarks;
    private String fieldType;
    /**
     * 数组字段的元素标准类型，非数组字段为空
     */
    private String fieldElementType;
    /**
     * 目标字段自动映射提示
     */
    private String fieldMappingMessage;
    private int fieldSize;
    private int fieldPrecision;
    private boolean fieldPk;
    private boolean fieldIndex;
    private Object defaultValue;
}
