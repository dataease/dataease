package io.dataease.api.visualization.dto;

import io.dataease.api.dataset.dto.DatasetTableFieldDTO;
import io.dataease.api.visualization.vo.VisualizationLinkageFieldVO;
import io.dataease.api.visualization.vo.VisualizationLinkageVO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : WangJiaHao
 * @date : 2023/7/13
 */
@Data
public class VisualizationLinkageDTO extends VisualizationLinkageVO {

    /**
     * 目标视图名称
     */
    private String targetViewName;
    /**
     * 联动字段
     */
    private List<VisualizationLinkageFieldVO> linkageFields = new ArrayList<>();

    /**
     * 目标视图字段
     */
    private List<DatasetTableFieldDTO> targetViewFields = new ArrayList<>();
    /**
     * 表ID
     */
    private String tableId;

}
