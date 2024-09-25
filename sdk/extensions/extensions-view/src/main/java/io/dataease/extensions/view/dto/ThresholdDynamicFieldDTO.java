package io.dataease.extensions.view.dto;

import lombok.Data;

/**
 * @author jianneng
 * @date 2024/9/19 18:31
 **/
@Data
public class ThresholdDynamicFieldDTO {
    /**
     * 字段id
     */
    private String fieldId;
    /**
     * 字段
     */
    private ChartViewFieldDTO field;
    /**
     * 条件
     */
    private String summary;
}
