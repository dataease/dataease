package io.dataease.api.visualization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dataease.api.visualization.request.VisualizationDraftRevision;
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

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private VisualizationDraftRevision revision;

    //仪表板可以跳转图表信息 sourceViewId#
    private String sourceInfo;

    private String targetInfo;

    private List<String> targetInfoList;

    private List<VisualizationLinkJumpInfoDTO> linkJumpInfoArray = new ArrayList<>();

    private Map<String,VisualizationLinkJumpInfoDTO> mapJumpInfoArray = new HashMap<>();
}
