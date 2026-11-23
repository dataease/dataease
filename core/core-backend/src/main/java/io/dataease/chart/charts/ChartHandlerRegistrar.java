package io.dataease.chart.charts;

import io.dataease.extensions.view.plugin.AbstractChartPlugin;
import io.dataease.utils.CommonBeanFactory;

/**
 * 图表 handler 静态注册入口，避免各 handler 反向注入 ChartHandlerManager。
 */
public final class ChartHandlerRegistrar {

    private ChartHandlerRegistrar() {
    }

    public static void register(String render, String type, AbstractChartPlugin chartHandler) {
        CommonBeanFactory.getBean(ChartHandlerManager.class).registerChartHandler(render, type, chartHandler);
    }
}
