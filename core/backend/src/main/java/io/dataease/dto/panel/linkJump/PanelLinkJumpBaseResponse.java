package io.dataease.dto.panel.linkJump;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Author: wangjiahao
 * Date: 2021/10/28
 * Description:
 */
@Data
public class PanelLinkJumpBaseResponse {

    // 获取基础的所有映射
    private Map<String, PanelLinkJumpInfoDTO> baseJumpInfoMap;

    // 获取仪表板类型映射
    private Map<String, List<String>> baseJumpInfoPanelMap;


    public PanelLinkJumpBaseResponse(Map<String, PanelLinkJumpInfoDTO> baseJumpInfoMap, Map<String, List<String>> baseJumpInfoPanelMap) {
        this.baseJumpInfoMap = baseJumpInfoMap;
        this.baseJumpInfoPanelMap = baseJumpInfoPanelMap;
    }

    public PanelLinkJumpBaseResponse() {
    }
}
