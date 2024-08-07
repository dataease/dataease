package io.dataease.extensions.datasource.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SimpleDatasourceDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 2255370172449547802L;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pid;
    private String name;
    private String type;
    private String typeAlias;
    private String status;
    private Boolean enableDataFill;
}
