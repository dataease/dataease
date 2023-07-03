package io.dataease.api.chart;

import io.dataease.api.chart.dto.ChartViewDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Author Junjun
 */
public interface ChartDataApi {
    @PostMapping("getData")
    ChartViewDTO getData(@RequestBody ChartViewDTO chartViewDTO) throws Exception;

    @PostMapping("getFieldData/{fieldId}/{fieldType}")
    List<String> getFieldData(@RequestBody ChartViewDTO view, @PathVariable Long fieldId, @PathVariable String fieldType) throws Exception;
}
