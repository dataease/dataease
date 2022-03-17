package io.dataease.dto.panel.outerParams;

import io.dataease.base.domain.PanelOuterParamsInfo;
import io.dataease.base.domain.PanelOuterParamsTargetViewInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2022/3/17
 * Description:
 */
@Data
public class PanelOuterParamsInfoDTO extends PanelOuterParamsInfo {

    private String panelId;

    private List<PanelOuterParamsTargetViewInfo> targetViewInfoList=new ArrayList<>();
}
