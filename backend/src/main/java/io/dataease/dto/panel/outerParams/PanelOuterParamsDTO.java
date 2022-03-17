package io.dataease.dto.panel.outerParams;

import io.dataease.base.domain.PanelOuterParams;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: wangjiahao
 * Date: 2022/3/17
 * Description:
 */
@Data
public class PanelOuterParamsDTO extends PanelOuterParams {

    private List<String> targetInfoList;

    private List<PanelOuterParamsInfoDTO> outerParamsInfoArray = new ArrayList<>();

    private Map<String,PanelOuterParamsInfoDTO> mapOuterParamsInfoArray = new HashMap<>();
}
