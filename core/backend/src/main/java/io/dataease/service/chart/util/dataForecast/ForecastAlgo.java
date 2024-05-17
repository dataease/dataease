package io.dataease.service.chart.util.dataForecast;

import io.dataease.dto.chart.ChartSeniorForecastDTO;
import io.dataease.dto.chart.ChartViewDTO;
import io.dataease.dto.chart.ForecastDataDTO;
import lombok.Getter;

import java.util.List;

@Getter
public abstract class ForecastAlgo {
    private final String algoType;

    public ForecastAlgo() {
        this.algoType = this.getAlgoType();
        ForecastAlgoManager.register(this);
    }

    public abstract List<ForecastDataDTO> forecast(ChartSeniorForecastDTO forecastCfg, List<String[]> data, ChartViewDTO view);

}
