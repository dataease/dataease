package io.dataease.api.permissions.variable.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

@Data
public class SysVariableValueItem {
    private SysVariableDto sysVariableDto;
    @JsonSerialize(using = ToStringSerializer.class)
    private String variableId;
    @JsonSerialize(using = ToStringSerializer.class)
    private String variableValueId;
    @JsonSerialize(using = ToStringSerializer.class)
    private String variableValue;
    private List<SysVariableValueDto> valueList;
}
