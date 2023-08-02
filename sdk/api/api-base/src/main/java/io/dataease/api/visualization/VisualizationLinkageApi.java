package io.dataease.api.visualization;

import io.dataease.api.commons.BaseRspModel;
import io.dataease.api.visualization.request.VisualizationLinkageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * @author : WangJiaHao
 * @date : 2023/7/13
 */
public interface VisualizationLinkageApi {


    @PostMapping("/getViewLinkageGather")
    Map getViewLinkageGather(@RequestBody VisualizationLinkageRequest request);

    @PostMapping("/getViewLinkageGatherArray")
    List getViewLinkageGatherArray(@RequestBody VisualizationLinkageRequest request);

    @PostMapping("/saveLinkage")
    BaseRspModel saveLinkage(@RequestBody VisualizationLinkageRequest request);

    @GetMapping("/getVisualizationAllLinkageInfo/{dvId}")
    Map<String, List<String>> getVisualizationAllLinkageInfo(@PathVariable String dvId);

}
