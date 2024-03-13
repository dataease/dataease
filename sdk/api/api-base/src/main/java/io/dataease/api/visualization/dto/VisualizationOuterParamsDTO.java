package io.dataease.api.visualization.dto;

import io.dataease.api.visualization.vo.VisualizationOuterParamsVO;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
public class VisualizationOuterParamsDTO extends VisualizationOuterParamsVO {

    private List<String> targetInfoList;

    private List<VisualizationOuterParamsInfoDTO> outerParamsInfoArray = new ArrayList<>();

    private Map<String,VisualizationOuterParamsInfoDTO> mapOuterParamsInfoArray = new HashMap<>();

}
