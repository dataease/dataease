package io.dataease.api.permissions.variable.dto;

import lombok.Data;

@Data
public class SysVariableValueItem {
    private String variable;
    private String variableValue;
    private String variableValueId;
}
