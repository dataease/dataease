package io.dataease.api.visualization;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.visualization.dto.VisualizationComponentDTO;
import io.dataease.api.visualization.dto.VisualizationLinkJumpDTO;
import io.dataease.api.visualization.request.VisualizationLinkJumpBaseRequest;
import io.dataease.api.visualization.response.VisualizationLinkJumpBaseResponse;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author : WangJiaHao
 * @date : 2023/7/13
 */
@Tag(name = "可视化管理:跳转")
@ApiSupport(order = 995)
public interface VisualizationLinkJumpApi {


    @GetMapping("/getTableFieldWithViewId/{viewId}")
    @Operation(summary = "查询可跳转字段信息")
    List<DatasetTableFieldDTO> getTableFieldWithViewId(@PathVariable Long viewId);

    @GetMapping("/queryWithViewId/{dvId}/{viewId}")
    @Operation(summary = "根据图表ID查询跳转信息")
    VisualizationLinkJumpDTO queryWithViewId(@PathVariable Long dvId, @PathVariable Long viewId);

    @GetMapping("/queryVisualizationJumpInfo/{dvId}")
    @Operation(summary = "根据可视化资源ID查询跳转信息")
    VisualizationLinkJumpBaseResponse queryVisualizationJumpInfo(@PathVariable Long dvId);

    @PostMapping("/updateJumpSet")
    @Operation(summary = "更新跳转信息")
    void updateJumpSet(@RequestBody VisualizationLinkJumpDTO jumpDTO);

    @PostMapping("/queryTargetVisualizationJumpInfo")
    @Operation(summary = "查询目标跳转信息")
    VisualizationLinkJumpBaseResponse queryTargetVisualizationJumpInfo(@RequestBody VisualizationLinkJumpBaseRequest request);

    @GetMapping("/viewTableDetailList/{dvId}")
    @Operation(summary = "查询跳转明细")
    VisualizationComponentDTO viewTableDetailList(@PathVariable Long dvId);

    @PostMapping("/updateJumpSetActive")
    @Operation(summary = "更新跳转信息可用状态")
    VisualizationLinkJumpBaseResponse updateJumpSetActive(@RequestBody VisualizationLinkJumpBaseRequest request);
}
