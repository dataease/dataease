package io.dataease.api.permissions.dataset.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class Item {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String name;
}
