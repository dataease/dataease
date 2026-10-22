package io.dataease.api.material.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaterialVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String name;
    private String format;
    private long size;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long categoryId;
    private boolean preset;
    private String reference;
}
