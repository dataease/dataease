package io.dataease.api.visualization;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.visualization.request.VisualizationSubjectRequest;
import io.dataease.api.visualization.vo.VisualizationSubjectVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "可视化管理:主题")
@ApiSupport(order = 993)
public interface VisualizationSubjectApi {

    @PostMapping("/query")
    @Operation(summary = "查询")
    List<VisualizationSubjectVO> query(@RequestBody VisualizationSubjectRequest request);

    @PostMapping("/querySubjectWithGroup")
    @Operation(summary = "分组查询")
    List<VisualizationSubjectVO> querySubjectWithGroup(@RequestBody VisualizationSubjectRequest request);

    @PostMapping("/update")
    @Operation(summary = "更新")
    void update(@RequestBody VisualizationSubjectRequest request);

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除")
    void delete(@PathVariable String id);

}
