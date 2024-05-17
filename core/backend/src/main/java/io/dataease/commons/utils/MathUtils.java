package io.dataease.commons.utils;

import groovy.lang.Tuple2;
import org.apache.commons.math4.legacy.stat.regression.SimpleRegression;
import org.apache.commons.statistics.distribution.NormalDistribution;
import org.apache.commons.statistics.distribution.TDistribution;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class MathUtils {
    private static final NormalDistribution NORMAL_DISTRIBUTION = NormalDistribution.of(0.0, 1.0);

    /**
     * 获取百分比
     * 保留一位小数
     *
     * @param value
     * @return
     */
    public static double getPercentWithDecimal(double value) {
        return new BigDecimal(value * 100).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 获取预测数据的置信区间，这边计算的是预测值的置信区间，还有一个是预测值的预测区间，公式不一样，注意区分.
     * 参考资料 <a href="https://zhuanlan.zhihu.com/p/366307027">知乎</a>,
     * <a href="https://real-statistics.com/regression/confidence-and-prediction-intervals/">real-statistics</a>
     * @param data 原始数据
     * @param forecastValue 预测得到的数据
     * @param forecastData 将原数据 x 代入回归方程得到的拟合数据
     * @param alpha 置信水平
     * @param degreeOfFreedom 自由度，t分布使用
     * @return 预测值的置信区间数组
     */
    public static double[][] getConfidenceInterval(double[][] data, double[] forecastValue, double[][] forecastData, double alpha, int degreeOfFreedom) {
        // y 平均方差
        double totalPow = 0;
        double xTotal = 0;
        for (int i = 0; i < data.length; i++) {
            double xVal = data[i][0];
            xTotal += xVal;
            double realVal = data[i][1];
            double predictVal = forecastValue[i];
            totalPow += Math.pow((realVal - predictVal), 2);
        }
        double xAvg = xTotal / data.length;
        double yMseSqrt = Math.sqrt(totalPow / (forecastValue.length - 2));
        // x 均值方差
        double xSubPow = 0;
        for (int i = 0; i < data.length; i++) {
            double xVal = data[i][0];
            xSubPow += Math.pow(xVal - xAvg, 2);
        }
        // t/z 值, 样本数 < 30 选 t 分布， > 30 选 z 分布,
        double tzFactor;
        if (data.length <= 30) {
            tzFactor = TDistribution.of(degreeOfFreedom).inverseCumulativeProbability(1 - (1 - alpha) / 2);
        } else {
            tzFactor = NORMAL_DISTRIBUTION.inverseCumulativeProbability(1 - (1 - alpha) / 2);
        }
        double[][] result = new double[forecastData.length][2];
        for (int i = 0; i < forecastData.length; i++) {
            double xVal = forecastData[i][0];
            double curSubPow = Math.pow(xVal - xAvg, 2);
            double sqrt = Math.sqrt(1.0 / data.length + curSubPow / xSubPow);
            double lower = forecastData[i][1] - tzFactor * yMseSqrt * sqrt;
            double upper = forecastData[i][1] + tzFactor * yMseSqrt * sqrt;
            result[i][0] = lower;
            result[i][1] = upper;
        }
        return result;
    }

}
