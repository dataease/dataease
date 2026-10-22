package io.dataease.api.material.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MaterialCategoryVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String name;
    private long count;
}
