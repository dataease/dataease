package io.dataease.extensions.datasource.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;
import java.io.Serializable;

@Slf4j
@Getter
@Setter
@Accessors(chain = true)
public class TableFieldWithValue implements Serializable {
    @Serial
    private static final long serialVersionUID = -8852504196142402103L;

    private Object value;
    private String filedName;
    private String typeName;
    private Integer type;
    private String columnTypeName;
    private boolean autoIncrement;

    private String isDateTime;
    private String dateFormat;
    private String term = "eq";
    private Integer deExtractType;

    public static TableFieldWithValue copy(TableFieldWithValue tableFieldWithValue) {
        return new TableFieldWithValue()
                .setValue(tableFieldWithValue.getValue())
                .setAutoIncrement(tableFieldWithValue.isAutoIncrement())
                .setFiledName(tableFieldWithValue.getFiledName())
                .setTypeName(tableFieldWithValue.getTypeName())
                .setType(tableFieldWithValue.getType())
                .setColumnTypeName(tableFieldWithValue.getColumnTypeName())
                .setIsDateTime(tableFieldWithValue.getIsDateTime())
                .setDateFormat(tableFieldWithValue.getDateFormat())
                .setTerm(tableFieldWithValue.getTerm())
                .setDeExtractType(tableFieldWithValue.getDeExtractType());
    }
}
