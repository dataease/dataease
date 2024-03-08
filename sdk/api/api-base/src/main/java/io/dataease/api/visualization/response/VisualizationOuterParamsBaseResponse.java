package io.dataease.api.visualization.response;

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

    public VisualizationOuterParamsBaseResponse(Map<String, List<String>> outerParamsInfoMap) {
        this.outerParamsInfoMap = outerParamsInfoMap;
    }

    public VisualizationOuterParamsBaseResponse() {
    }
}
