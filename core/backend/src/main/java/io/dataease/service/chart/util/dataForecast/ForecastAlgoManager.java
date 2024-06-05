package io.dataease.service.chart.util.dataForecast;

import io.dataease.service.chart.util.dataForecast.impl.LinearRegressionAlgo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ForecastAlgoManager {
    private static final Map<String, ForecastAlgo> FORECAST_ALGO_MAP = new ConcurrentHashMap<>();
    public static ForecastAlgo getAlgo(String algoType) {
        return FORECAST_ALGO_MAP.get(algoType);
    }

    public static void register(ForecastAlgo forecastAlgo) {
        FORECAST_ALGO_MAP.put(forecastAlgo.getAlgoType(), forecastAlgo);
    }
}
