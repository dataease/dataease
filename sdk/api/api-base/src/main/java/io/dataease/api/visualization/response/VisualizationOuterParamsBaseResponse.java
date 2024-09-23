package io.dataease.api.visualization.response;

import io.dataease.api.visualization.dto.VisualizationOuterParamsInfoDTO;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Author: wangjiahao
 * Description:
 */
@Data
public class VisualizationOuterParamsBaseResponse {

    // 获取仪表板外部参数映射信息
    private Map<String, List<String>> outerParamsInfoMap;

    private Map<String,VisualizationOuterParamsInfoDTO> outerParamsInfoBaseMap;

    public VisualizationOuterParamsBaseResponse(Map<String, List<String>> outerParamsInfoMap,Map<String,VisualizationOuterParamsInfoDTO> outerParamsInfoBaseMap) {
        this.outerParamsInfoMap = outerParamsInfoMap;
        this.outerParamsInfoBaseMap = outerParamsInfoBaseMap;
    }

    public VisualizationOuterParamsBaseResponse() {
    }
}
