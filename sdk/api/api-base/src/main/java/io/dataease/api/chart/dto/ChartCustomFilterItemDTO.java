package io.dataease.api.chart.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author Junjun
 */
@Data
public class ChartCustomFilterItemDTO implements Serializable {
    private String fieldId;
    private String term;
    private String value;
}
