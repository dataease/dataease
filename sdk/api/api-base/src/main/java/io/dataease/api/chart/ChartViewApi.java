package io.dataease.api.chart;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.chart.dto.ChartViewDTO;
import io.dataease.api.chart.dto.ChartViewFieldDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * @Author Junjun
 */
@Tag(name = "视图管理:查看")
@ApiSupport(order = 988)
public interface ChartViewApi {
    @Operation(summary = "查询视图详情并同时计算数据", hidden = true)
    @PostMapping("getChart/{id}")
    ChartViewDTO getData(@PathVariable Long id) throws Exception;

    @Operation(summary = "获取视图字段")
    @PostMapping("listByDQ/{id}/{chartId}")
    Map<String, List<ChartViewFieldDTO>> listByDQ(@PathVariable Long id, @PathVariable Long chartId);

    @Operation(summary = "保存视图")
    @PostMapping("save")
    ChartViewDTO save(@RequestBody ChartViewDTO dto) throws Exception;

    @Operation(summary = "检查是否同数据集")
    @GetMapping("/checkSameDataSet/{viewIdSource}/{viewIdTarget}")
    String checkSameDataSet(@PathVariable String viewIdSource, @PathVariable String viewIdTarget);

    @Operation(summary = "查询视图详情")
    @PostMapping("getDetail/{id}")
    ChartViewDTO getDetail(@PathVariable Long id);
}
