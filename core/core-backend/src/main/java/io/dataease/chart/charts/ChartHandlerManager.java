package io.dataease.chart.charts;

import io.dataease.chart.charts.impl.DefaultChartHandler;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChartHandlerManager {
    @Lazy
    @Resource
    private DefaultChartHandler defaultChartHandler;
    private static final ConcurrentHashMap<String, AbstractChartHandler> CHART_HANDLER_MAP = new ConcurrentHashMap<>();

    public void registerChartHandler(String render, String type, AbstractChartHandler chartHandler) {
        CHART_HANDLER_MAP.put(render + "-" + type, chartHandler);
    }

    public AbstractChartHandler getChartHandler(String render, String type) {
        var handler =  CHART_HANDLER_MAP.get(render + "-" + type);
        if (handler == null) {
            return defaultChartHandler;
        }
        return handler;
    }
}
