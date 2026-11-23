package io.dataease.chart.charts;

import io.dataease.chart.charts.impl.DefaultChartHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 图表插件注册器：负责把默认图表 handler 注册到 ChartHandlerManager。
 * 使 DefaultChartHandler 不再反向依赖 ChartHandlerManager，打破插件注册环。
 */
@Component
public class ChartPluginRegistrar {

    @Resource
    private ChartHandlerManager chartHandlerManager;
    @Resource(name = "defaultChartHandler")
    private DefaultChartHandler defaultChartHandler;

    @PostConstruct
    public void init() {
        chartHandlerManager.registerChartHandler(defaultChartHandler.getRender(), defaultChartHandler.getType(), defaultChartHandler);
    }
}
