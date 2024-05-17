package io.dataease.dto.chart;

import lombok.Data;

@Data
public class ChartSeniorForecastDTO {
    /**
     * 是否开启预测
     */
    private boolean enable;
    /**
     * 预测周期
     */
    private int period;
    /**
     * 是否使用所有数据进行预测
     */
    private boolean allPeriod;
    /**
     * 用于预测的数据量
     */
    private int trainingPeriod;
    /**
     * 置信区间
     */
    private float confidenceInterval;
    /**
     * 预测用的算法/模型
     */
    private String algorithm;
    /**
     * 多项式阶数
     */
    private int degree;
}
