package io.dataease.api.visualization.dto;

import io.dataease.api.visualization.vo.VisualizationViewTableVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author : WangJiaHao
 * @date : 2024/4/18 17:14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisualizationComponentDTO {

    private String bashComponentData;

    List<VisualizationViewTableVO> visualizationViewTables;

}
