package io.dataease.api.chart;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.chart.dto.ChartViewDTO;
import io.dataease.api.chart.dto.ChartViewFieldDTO;
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
    @PostMapping("getChart/{id}")
    ChartViewDTO getData(@PathVariable Long id) throws Exception;

    @PostMapping("listByDQ/{id}/{chartId}")
    Map<String, List<ChartViewFieldDTO>> listByDQ(@PathVariable Long id, @PathVariable Long chartId);

    @PostMapping("save")
    ChartViewDTO save(@RequestBody ChartViewDTO dto) throws Exception;

    @GetMapping("/checkSameDataSet/{viewIdSource}/{viewIdTarget}")
    String checkSameDataSet(@PathVariable String viewIdSource, @PathVariable String viewIdTarget);

    @PostMapping("getDetail/{id}")
    ChartViewDTO getDetail(@PathVariable Long id);
}
