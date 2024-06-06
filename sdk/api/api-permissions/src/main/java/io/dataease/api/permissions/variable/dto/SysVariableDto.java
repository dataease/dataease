package io.dataease.api.permissions.variable.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class SysVariableDto {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String type;
    private String name;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long min;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long max;
    private String startTime;
    private String endTime;
    private boolean root = false;
    private boolean disabled = false;
}

