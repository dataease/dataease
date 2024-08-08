package io.dataease.extensions.datasource.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author Junjun
 */
@Data
public class CalParam implements Serializable {
    @JsonSerialize(using = ToStringSerializer.class)
    private String id;
    private String name;
    private String value;
}
