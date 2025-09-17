package io.dataease.api.visualization.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class VisualizationOutParamsJumpVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String type;

    private String name;

    private String title;
}
