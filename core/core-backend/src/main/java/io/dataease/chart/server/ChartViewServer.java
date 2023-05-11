package io.dataease.chart.server;

import io.dataease.api.chart.ChartViewApi;
import io.dataease.api.chart.dto.ChartViewDTO;
import io.dataease.chart.manage.ChartViewManege;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author Junjun
 */
@RestController
@RequestMapping("chart")
public class ChartViewServer implements ChartViewApi {
    @Resource
    private ChartViewManege chartViewManege;

    @Override
    public ChartViewDTO getData(Long id) throws Exception {
        return chartViewManege.getChart(id);
    }
}
