package io.dataease.api.permissions.auth.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class BusiResourceCreator implements Serializable {
    @Serial
    private static final long serialVersionUID = 3656747026193567757L;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String name;
    private String flag;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pid = 0L;
    private Boolean leaf;
    private int extraFlag;
}
