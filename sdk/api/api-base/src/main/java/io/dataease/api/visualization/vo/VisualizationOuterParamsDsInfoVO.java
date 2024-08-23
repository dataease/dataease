package io.dataease.api.visualization.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class VisualizationOuterParamsDsInfoVO {

    private String dsName;

    private String dsId;

    private List targetFieldInfo;

    private Map<String,Boolean> viewCheckedInfo;

}
