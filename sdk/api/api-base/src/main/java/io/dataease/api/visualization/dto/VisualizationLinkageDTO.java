package io.dataease.api.visualization.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.dataease.api.visualization.vo.VisualizationLinkageFieldVO;
import io.dataease.api.visualization.vo.VisualizationLinkageVO;
import io.dataease.dto.dataset.DatasetTableFieldDTO;
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
     * 目标视图类型
     */
    private String targetViewType;
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
    @JsonSerialize(using = ToStringSerializer.class)
    private Long tableId;

}
