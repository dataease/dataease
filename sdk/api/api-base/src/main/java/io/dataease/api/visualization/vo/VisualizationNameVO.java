package io.dataease.api.visualization.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VisualizationNameVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String name;
}
