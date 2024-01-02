package io.dataease.api.visualization;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.visualization.request.VisualizationStoreRequest;
import io.dataease.api.visualization.request.VisualizationWorkbranchQueryRequest;
import io.dataease.api.visualization.vo.VisualizationStoreVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "可视化管理:收藏")
@ApiSupport(order = 994)
public interface VisualizationStoreApi {

    @PostMapping("/execute")
    @Operation(summary = "变更收藏信息")
    void execute(@RequestBody VisualizationStoreRequest request);

    @PostMapping("/query")
    @Operation(summary = "查询收藏资源信息")
    List<VisualizationStoreVO> query(@RequestBody VisualizationWorkbranchQueryRequest request);
    @GetMapping("/favorited/{id}")
    @Operation(summary = "收藏")
    boolean favorited(@PathVariable("id") Long id);
}
