package io.dataease.commons.utils;

import io.dataease.extensions.datasource.dto.TableFieldWithValue;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SqlVariableHandleResult {
    private String sql;
    private List<TableFieldWithValue> tableFieldWithValues = new ArrayList<>();

    public SqlVariableHandleResult(String sql) {
        this.sql = sql;
    }
}
