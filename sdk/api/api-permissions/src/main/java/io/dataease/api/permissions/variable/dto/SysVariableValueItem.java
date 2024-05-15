package io.dataease.api.permissions.variable.dto;

import lombok.Data;

import java.util.List;

@Data
public class SysVariableValueItem {
    private SysVariableDto sysVariableDto;
    private String variableId;
    private String variableValueId;
    private String variableValue;
    private List<SysVariableValueDto> valueList;
}
