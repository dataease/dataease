package io.dataease.api.visualization;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.visualization.request.VisualizationBackgroundRequest;
import io.dataease.api.visualization.vo.VisualizationBackgroundVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

/**
 * @author : WangJiaHao
 * @date : 2023/6/12 19:19
 */

@Tag(name = "可视化管理:背景")
@ApiSupport(order = 997)
public interface VisualizationBackgroundApi {
    @GetMapping("/findAll")
    @Operation(summary = "背景信息查询")
    Map<String, List<VisualizationBackgroundVO>> findAll();
}
