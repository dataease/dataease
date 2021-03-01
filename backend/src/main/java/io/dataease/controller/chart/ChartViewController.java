package io.dataease.controller.chart;

import io.dataease.base.domain.ChartViewWithBLOBs;
import io.dataease.controller.request.chart.ChartViewRequest;
import io.dataease.dto.chart.ChartViewDTO;
import io.dataease.service.chart.ChartViewService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @Author gin
 * @Date 2021/3/1 1:17 下午
 */
@RestController
@RequestMapping("/chart/view")
public class ChartViewController {
    @Resource
    private ChartViewService chartViewService;

    @PostMapping("/save")
    public ChartViewWithBLOBs save(@RequestBody ChartViewWithBLOBs chartViewWithBLOBs) {
        return chartViewService.save(chartViewWithBLOBs);
    }

    @PostMapping("/list")
    public List<ChartViewWithBLOBs> list(@RequestBody ChartViewRequest chartViewRequest) {
        return chartViewService.list(chartViewRequest);
    }

    @PostMapping("/get/{id}")
    public ChartViewWithBLOBs get(@PathVariable String id) {
        return chartViewService.get(id);
    }

    @PostMapping("/delete/{id}")
    public void delete(@PathVariable String id) {
        chartViewService.delete(id);
    }

    @PostMapping("/getData/{id}")
    public ChartViewDTO getData(@PathVariable String id) throws Exception {
        return chartViewService.getData(id);
    }
}
