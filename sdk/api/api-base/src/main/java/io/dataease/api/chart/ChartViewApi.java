package io.dataease.api.chart;

import io.dataease.api.chart.dto.ChartViewDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @Author Junjun
 */
public interface ChartViewApi {
    @PostMapping("getChart/{id}")
    ChartViewDTO getData(@PathVariable Long id) throws Exception;
}
