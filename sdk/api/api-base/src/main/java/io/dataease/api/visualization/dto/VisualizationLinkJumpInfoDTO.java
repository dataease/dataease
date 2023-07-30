package io.dataease.api.visualization.dto;

import io.dataease.api.visualization.vo.VisualizationLinkJumpInfoVO;
import io.dataease.api.visualization.vo.VisualizationLinkJumpTargetViewInfoVO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : WangJiaHao
 * @date : 2023/7/18 14:20
 */
@Data
public class VisualizationLinkJumpInfoDTO extends VisualizationLinkJumpInfoVO {
    private String sourceFieldName;

    private String sourceJumpInfo;

    private Integer sourceDeType;

    //存在公共链接的目标仪表板
    private String publicJumpId;

    private List<VisualizationLinkJumpTargetViewInfoVO> targetViewInfoList=new ArrayList<>();// linkType = inner 时使用

}
