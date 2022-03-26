package io.dataease.dto.panel.outerParams;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Author: wangjiahao
 * Date: 2022/3/25
 * Description:
 */
@Data
public class PanelOuterParamsBaseResponse {

    // 获取仪表板外部参数映射信息
    private Map<String, List<String>> outerParamsInfoMap;

    public PanelOuterParamsBaseResponse(Map<String, List<String>> outerParamsInfoMap) {
        this.outerParamsInfoMap = outerParamsInfoMap;
    }

    public PanelOuterParamsBaseResponse() {
    }
}
