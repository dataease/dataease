package io.dataease.api.visualization.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class VisualizationOutParamsJumpVO {

    private String id;

    private String type;

    private String name;

    private String title;
}
