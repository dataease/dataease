package io.dataease.api.visualization;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.commons.BaseRspModel;
import io.dataease.api.visualization.request.VisualizationLinkageRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "可视化管理:联动")
@ApiSupport(order = 996)
public interface VisualizationLinkageApi {


    @PostMapping("/getViewLinkageGather")
    @Operation(summary = "查询联动信息")
    Map getViewLinkageGather(@RequestBody VisualizationLinkageRequest request);

    @PostMapping("/getViewLinkageGatherArray")
    @Operation(summary = "查询联动信息数组")
    List getViewLinkageGatherArray(@RequestBody VisualizationLinkageRequest request);

    @PostMapping("/saveLinkage")
    @Operation(summary = "保存联动信息")
    BaseRspModel saveLinkage(@RequestBody VisualizationLinkageRequest request);

    @GetMapping("/getVisualizationAllLinkageInfo/{dvId}")
    @Operation(summary = "根据资源ID查询联动信息")
    Map<String, List<String>> getVisualizationAllLinkageInfo(@PathVariable Long dvId);

    @PostMapping("/updateLinkageActive")
    @Operation(summary = "修改联动信息可用状态")
    Map updateLinkageActive(@RequestBody VisualizationLinkageRequest request);

}
