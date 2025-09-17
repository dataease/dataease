package io.dataease.api.visualization.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class VisualizationOuterParamsDsInfoVO {

    private String dsName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long dsId;

    private List targetFieldInfo;

    private Map<String,Boolean> viewCheckedInfo;

}
