package io.dataease.api.visualization.dto;

import io.dataease.api.visualization.vo.VisualizationLinkJumpVO;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : WangJiaHao
 * @date : 2023/7/18 14:21
 */
@Data
public class VisualizationLinkJumpDTO extends VisualizationLinkJumpVO {
    //仪表板可以跳转视图信息 sourceViewId#
    private String sourceInfo;

    private List<String> targetInfoList;

    private List<VisualizationLinkJumpInfoDTO> linkJumpInfoArray = new ArrayList<>();

    private Map<String,VisualizationLinkJumpInfoDTO> mapJumpInfoArray = new HashMap<>();
}
