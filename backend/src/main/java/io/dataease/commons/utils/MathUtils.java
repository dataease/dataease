package io.dataease.commons.utils;

import java.math.BigDecimal;

public class MathUtils {

    /**
     * 获取百分比
     * 保留一位小数
     *
     * @param value
     * @return
     */
    public static double getPercentWithDecimal(double value) {
        return new BigDecimal(value * 100)
                .setScale(1, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }
}
