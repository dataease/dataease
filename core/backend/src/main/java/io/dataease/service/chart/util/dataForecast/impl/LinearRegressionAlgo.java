package io.dataease.service.chart.util.dataForecast.impl;

import com.alibaba.fastjson.JSONObject;
import io.dataease.commons.utils.MathUtils;
import io.dataease.dto.chart.ChartSeniorForecastDTO;
import io.dataease.dto.chart.ChartViewDTO;
import io.dataease.dto.chart.ForecastDataDTO;
import io.dataease.plugins.common.dto.chart.ChartViewFieldDTO;
import io.dataease.service.chart.util.dataForecast.ForecastAlgo;
import lombok.Getter;
import org.apache.commons.math4.legacy.stat.regression.SimpleRegression;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Getter
public class LinearRegressionAlgo extends ForecastAlgo {

    private final String algoType = "linear-regression";

    @Override
    public List<ForecastDataDTO> forecast(ChartSeniorForecastDTO forecastCfg, List<String[]> data, ChartViewDTO view) {
        List<ChartViewFieldDTO> xAxis = JSONObject.parseArray(view.getXAxis(), ChartViewFieldDTO.class);
        final List<double[]> forecastData = new ArrayList<>(data.size());
        // 先按连续的数据处理
        for (int i = 0; i < data.size(); i++) {
            String val = data.get(i)[xAxis.size()];
            double value = Double.parseDouble(val);
            forecastData.add(new double[]{i, value});
        }
        double[][] matrix = forecastData.toArray(double[][]::new);
        SimpleRegression regression = new SimpleRegression();
        regression.addData(matrix);
        double[][] forecastMatrix = new double[forecastCfg.getPeriod()][2];
        for (int i = 0; i < forecastCfg.getPeriod(); i++) {
            double xVal = data.size() + i;
            double predictVal = regression.predict(xVal);
            forecastMatrix[i] =  new double[]{xVal, predictVal};
        }
        final double[] forecastValue = new double[forecastData.size()];
        for (int i = 0; i < forecastData.size(); i++) {
            double xVal = forecastData.get(i)[0];
            forecastValue[i] = regression.predict(xVal);
        }
        double[][] confidenceInterval = MathUtils.getConfidenceInterval(forecastData.toArray(new double[0][0]), forecastValue, forecastMatrix, forecastCfg.getConfidenceInterval(), forecastData.size() - 2);
        final List<ForecastDataDTO> result = new ArrayList<>(forecastCfg.getPeriod());
        for (int i = 0; i < forecastMatrix.length; i++) {
            ForecastDataDTO tmp = new ForecastDataDTO();
            tmp.setXVal(forecastMatrix[i][0]);
            tmp.setYVal(forecastMatrix[i][1]);
            tmp.setLower(confidenceInterval[i][0]);
            tmp.setUpper(confidenceInterval[i][1]);
            result.add(tmp);
        }
        return result;
    }
}
