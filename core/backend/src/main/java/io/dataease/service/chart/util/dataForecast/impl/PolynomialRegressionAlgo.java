package io.dataease.service.chart.util.dataForecast.impl;

import com.alibaba.fastjson.JSONObject;
import io.dataease.commons.utils.MathUtils;
import io.dataease.dto.chart.ChartSeniorForecastDTO;
import io.dataease.dto.chart.ChartViewDTO;
import io.dataease.dto.chart.ForecastDataDTO;
import io.dataease.plugins.common.dto.chart.ChartViewFieldDTO;
import io.dataease.service.chart.util.dataForecast.ForecastAlgo;
import lombok.Getter;
import org.apache.commons.math4.legacy.analysis.function.Logistic;
import org.apache.commons.math4.legacy.fitting.PolynomialCurveFitter;
import org.apache.commons.math4.legacy.fitting.WeightedObservedPoints;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Getter
public class PolynomialRegressionAlgo extends ForecastAlgo {
    private final String algoType = "polynomial-regression";

    @Override
    public List<ForecastDataDTO> forecast(ChartSeniorForecastDTO forecastCfg, List<String[]> data, ChartViewDTO view) {
        List<ChartViewFieldDTO> xAxis = JSONObject.parseArray(view.getXAxis(), ChartViewFieldDTO.class);
        WeightedObservedPoints points = new WeightedObservedPoints();
        double[][] originData = new double[data.size()][2];
        // 先按连续的数据处理
        for (int i = 0; i < data.size(); i++) {
            String val = data.get(i)[xAxis.size()];
            double value = Double.parseDouble(val);
            points.add(i, value);
            originData[i] = new double[]{i, value};
        }
        PolynomialCurveFitter filter = PolynomialCurveFitter.create(forecastCfg.getDegree());
        // 返回的是多次项系数, y = 3 + 2x + x*2 则为 [3,2,1]
        double[] coefficients = filter.fit(points.toList());
        double[][] forecastMatrix = new double[forecastCfg.getPeriod()][2];
        for (int i = 0; i < forecastCfg.getPeriod(); i++) {
            double xVal = data.size() + i;
            double predictVal = getPolynomialValue(xVal, coefficients);
            forecastMatrix[i] = new double[]{xVal, predictVal};
        }
        final double[] forecastValue = new double[data.size()];
        for (int i = 0; i < data.size(); i++) {
            double xVal = originData[i][0];
            forecastValue[i] = getPolynomialValue(xVal, coefficients);
        }
        int df = data.size() - forecastCfg.getDegree() - 1;
        double[][] confidenceInterval = MathUtils.getConfidenceInterval(originData, forecastValue, forecastMatrix, forecastCfg.getConfidenceInterval(), df);
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

    private double getPolynomialValue(double x, double[] coefficients) {
        double result = 0.0;
        for (int i = 0; i < coefficients.length; i++) {
            result += coefficients[i] * Math.pow(x, i);
        }
        return result;
    }
}
