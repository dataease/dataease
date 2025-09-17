package io.dataease.api.visualization.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class VisualizationOuterParamsFilterInfoVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private  Long filterComponentId;

    @JsonSerialize(using = ToStringSerializer.class)
    private  Long filterId;

}
