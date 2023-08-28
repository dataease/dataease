package io.dataease.api.visualization.request;

import io.dataease.api.visualization.dto.VisualizationLinkageDTO;
import io.dataease.api.visualization.vo.VisualizationLinkageVO;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : WangJiaHao
 * @date : 2023/7/13
 */
@Data
public class VisualizationLinkageRequest extends VisualizationLinkageVO {

    /**
     * 仪表板 or 大屏ID
     * */
    private String dvId;

    private String sourceViewId;

    private Boolean ActiveStatus;

    private List<String> targetViewIds;


    private List<VisualizationLinkageDTO> linkageInfo = new ArrayList<>();

}
