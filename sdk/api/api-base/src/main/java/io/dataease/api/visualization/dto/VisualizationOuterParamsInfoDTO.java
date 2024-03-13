package io.dataease.api.visualization.dto;

import io.dataease.api.visualization.vo.VisualizationOuterParamsInfoVO;
import io.dataease.api.visualization.vo.VisualizationOuterParamsTargetViewInfoVO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class VisualizationOuterParamsInfoDTO extends VisualizationOuterParamsInfoVO {
    private String dvId;

    private List<VisualizationOuterParamsTargetViewInfoVO> targetViewInfoList=new ArrayList<>();

    //仪表板外部参数信息 dvId#paramName
    private String sourceInfo;

    //目标联动参数 targetViewId#targetFieldId
    private List<String> targetInfoList;

}
