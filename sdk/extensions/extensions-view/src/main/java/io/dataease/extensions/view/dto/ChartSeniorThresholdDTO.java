package io.dataease.extensions.view.dto;

import lombok.Data;

/**
 * @author jianneng
 * @date 2024/9/19 18:34
 **/
@Data
public class ChartSeniorThresholdDTO {
    /**
     * 对比方式
     */
    private String term;
    /**
     * 类型，固定值、动态值
     */
    private String type;
    /**
     * 动态值字段
     */
    private ThresholdDynamicFieldDTO dynamicField;
    /**
     * 动态值最小值字段 仅当term为between时使用
     */
    private ThresholdDynamicFieldDTO dynamicMinField;
    /**
     * 动态值最大值字段 仅当term为between时使用
     */
    private ThresholdDynamicFieldDTO dynamicMaxField;
}
