package io.dataease.extensions.view.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 过滤
 */
@Data
public class ChartViewFieldFilterDTO implements Serializable {
    /**
     * 条件
     */
    private String term;
    /**
     * 值
     */
    private String value;
}
