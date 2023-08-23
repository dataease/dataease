package io.dataease.plugins.common.dto.chart;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Author gin
 * @Date 2021/7/20 11:34 上午
 */
@Getter
@Setter
public class ChartCustomFilterItemDTO implements Serializable {
    private String fieldId;
    private String term;
    private String value;
}
