package io.dataease.chart.server;

import io.dataease.api.chart.ChartViewApi;
import io.dataease.api.chart.dto.ChartViewDTO;
import io.dataease.api.chart.dto.ChartViewFieldDTO;
import io.dataease.chart.manage.ChartViewManege;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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

    @Override
    public Map<String, List<ChartViewFieldDTO>> listByDQ(Long id, Long chartId) {
        return chartViewManege.listByDQ(id, chartId);
    }

    @Override
    public ChartViewDTO save(ChartViewDTO dto) throws Exception {
        return chartViewManege.save(dto);
    }


}
