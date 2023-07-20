package io.dataease.api.visualization;

import io.dataease.api.visualization.dto.VisualizationLinkJumpDTO;
import io.dataease.api.visualization.request.VisualizationLinkJumpBaseRequest;
import io.dataease.api.visualization.response.VisualizationLinkJumpBaseResponse;
import io.dataease.api.visualization.vo.VisualizationViewTableVO;
import io.dataease.dto.dataset.DatasetTableFieldDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author : WangJiaHao
 * @date : 2023/7/13
 */
public interface VisualizationLinkJumpApi {


    @GetMapping("/getTableFieldWithViewId/{viewId}")
    List<DatasetTableFieldDTO> getTableFieldWithViewId(@PathVariable String viewId);

    @GetMapping("/queryWithViewId/{dvId}/{viewId}")
    VisualizationLinkJumpDTO queryWithViewId(@PathVariable String dvId, @PathVariable String viewId);

    @GetMapping("/queryVisualizationJumpInfo/{dvId}")
    VisualizationLinkJumpBaseResponse queryVisualizationJumpInfo(@PathVariable String dvId);

    @PostMapping("/updateJumpSet")
    void updateJumpSet(@RequestBody VisualizationLinkJumpDTO jumpDTO);

    @PostMapping("/queryTargetVisualizationJumpInfo")
    VisualizationLinkJumpBaseResponse queryTargetVisualizationJumpInfo(@RequestBody VisualizationLinkJumpBaseRequest request);

    @GetMapping("/viewTableDetailList/{dvId}")
    List<VisualizationViewTableVO> viewTableDetailList(@PathVariable String dvId);
}
