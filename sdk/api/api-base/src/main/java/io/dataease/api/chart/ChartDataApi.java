package io.dataease.api.chart;

import io.dataease.api.chart.dto.ChartViewDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Author Junjun
 */
public interface ChartDataApi {
    @PostMapping("getData")
    ChartViewDTO getData(@RequestBody ChartViewDTO chartViewDTO) throws Exception;
}
