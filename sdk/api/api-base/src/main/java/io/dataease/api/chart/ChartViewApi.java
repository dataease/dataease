package io.dataease.api.chart;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.chart.vo.ViewSelectorVO;
import io.dataease.extensions.view.dto.ChartViewDTO;
import io.dataease.extensions.view.dto.ChartViewFieldDTO;
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
@Tag(name = "图表管理:查看")
@ApiSupport(order = 988)
public interface ChartViewApi {
    @Operation(summary = "查询图表详情并同时计算数据", hidden = true)
    @PostMapping("getChart/{id}")
    ChartViewDTO getData(@PathVariable Long id) throws Exception;

    @Operation(summary = "获取图表字段")
    @PostMapping("listByDQ/{id}/{chartId}")
    Map<String, List<ChartViewFieldDTO>> listByDQ(@PathVariable Long id, @PathVariable Long chartId, @RequestBody ChartViewDTO dto);

    @Operation(summary = "保存图表")
    @PostMapping("save")
    ChartViewDTO save(@RequestBody ChartViewDTO dto) throws Exception;

    @Operation(summary = "检查是否同数据集")
    @GetMapping("/checkSameDataSet/{viewIdSource}/{viewIdTarget}")
    String checkSameDataSet(@PathVariable String viewIdSource, @PathVariable String viewIdTarget);

    @Operation(summary = "查询图表详情")
    @PostMapping("getDetail/{id}")
    ChartViewDTO getDetail(@PathVariable Long id);

    @Operation(summary = "查询仪表板下视图项")
    @GetMapping("/viewOption/{resourceId}")
    List<ViewSelectorVO> viewOption(@PathVariable("resourceId") Long resourceId);

    @Operation(summary = "视图复制字段")
    @PostMapping("copyField/{id}/{chartId}")
    void copyField(@PathVariable Long id, @PathVariable Long chartId);

    @Operation(summary = "视图删除字段")
    @PostMapping("deleteField/{id}")
    void deleteField(@PathVariable Long id);

    @Operation(summary = "清空当前视图计算字段")
    @PostMapping("deleteFieldByChart/{chartId}")
    void deleteFieldByChart(@PathVariable Long chartId);
}
