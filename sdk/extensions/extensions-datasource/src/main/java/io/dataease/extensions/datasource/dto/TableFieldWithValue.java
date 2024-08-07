package io.dataease.extensions.datasource.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

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

    public static TableFieldWithValue copy(TableFieldWithValue tableFieldWithValue) {
        return new TableFieldWithValue()
                .setValue(tableFieldWithValue.getValue())
                .setFiledName(tableFieldWithValue.getFiledName())
                .setTypeName(tableFieldWithValue.getTypeName())
                .setType(tableFieldWithValue.getType());
    }
}
