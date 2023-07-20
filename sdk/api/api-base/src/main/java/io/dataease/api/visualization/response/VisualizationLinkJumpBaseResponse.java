package io.dataease.api.visualization.response;

import io.dataease.api.visualization.dto.VisualizationLinkJumpInfoDTO;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author : WangJiaHao
 * @date : 2023/7/18 14:24
 */
@Data
public class VisualizationLinkJumpBaseResponse {

    // 获取基础的所有映射
    private Map<String, VisualizationLinkJumpInfoDTO> baseJumpInfoMap;

    // 获取仪表板类型映射
    private Map<String, List<String>> baseJumpInfoVisualizationMap;


    public VisualizationLinkJumpBaseResponse(Map<String, VisualizationLinkJumpInfoDTO> baseJumpInfoMap, Map<String, List<String>> baseJumpInfoVisualizationMap) {
        this.baseJumpInfoMap = baseJumpInfoMap;
        this.baseJumpInfoVisualizationMap = baseJumpInfoVisualizationMap;
    }

    public VisualizationLinkJumpBaseResponse() {

    }
}
